package no.ntnu.item.ttm4160.sunspot;

import java.io.IOException;

import no.ntnu.item.ttm4160.sunspot.runtime.IStateMachine;
import no.ntnu.item.ttm4160.sunspot.runtime.Queue;
import no.ntnu.item.ttm4160.sunspot.runtime.Scheduler;
import no.ntnu.item.ttm4160.sunspot.runtime.Timer;
import no.ntnu.item.ttm4160.sunspot.communication.Message;
import no.ntnu.item.ttm4160.sunspot.communication.Communications;

public class CallerStateMachine implements IStateMachine {
	
	private static final String GIVEUP_TIMER = "giveUpTimer", SEND_AGAIN_TIMER = "sendAgainTimer";
	private static final String[] STATES = {"READY", "WAIT_RESPONSE", "SENDING"};
	private Timer giveUpTimer = new Timer(GIVEUP_TIMER);
	private Timer sendAgainTimer = new Timer(SEND_AGAIN_TIMER);
	
	protected String state = STATES[0];
	
	private String callee, caller;
	private String id;
	private Queue eventQueue;
	
	DeviceOperator operator;
	Communications communication;
	
	public CallerStateMachine(String id, String mac, DeviceOperator operator, Communications communication) {
		this.id = id;
		this.caller = mac; 
		this.operator = operator;
		this.communication = communication;
	}
	
	public String getId(){
		return this.id;
	}
	
	public Queue getEventQueue(){
		return this.eventQueue;
	}
	
	public boolean isTimerDoable(String timerId){
		if(timerId.equals(GIVEUP_TIMER)||timerId.equals(SEND_AGAIN_TIMER)){
			return true;
		}
		return false;
	}
	
	public int fire(Scheduler scheduler) {
		Object event = eventQueue.take();
		if(state==STATES[0]) {
			if(event instanceof Message) {
				Message msg = (Message)event;
				if(msg.getContent().equals(Message.button1Pressed)){
					giveUpTimer.start(scheduler, 500);
					Message send = new Message(caller,Message.BROADCAST_ADDRESS,Message.CanYouDisplayMyReadings);
					communication.sendRemoteMessage(send);
					state = STATES[1];
					System.out.println("broadcast sent...");
					return EXECUTE_TRANSITION;
				} else if(msg.getContent().equals(Message.ICanDisplayReadings)){
					callee = msg.getSender();
					Message send = new Message(caller,callee,Message.Denied);
					communication.sendRemoteMessage(send);
					System.out.println("request denied...");
					return EXECUTE_TRANSITION;
				}
			}
		} else if(state==STATES[1]) {
			if(event instanceof Message) {
				Message msg = (Message)event;
				if(msg.getContent().equals(Message.ICanDisplayReadings)){
					callee = msg.getSender();
					Message send = new Message(caller,callee,Message.Approved);
					communication.sendRemoteMessage(send);
					sendAgainTimer.start(scheduler, 100);
					state = STATES[2];
					System.out.println("approve and start sending...");
					return EXECUTE_TRANSITION;
				}
			} else if(event instanceof Timer){
				Timer timer = (Timer)event;
				if(timer.getId().equals(GIVEUP_TIMER)){
					operator.blinkLEDs();
					state = STATES[0];
					System.out.println("give up timer expires...");
					return EXECUTE_TRANSITION;
				}
			}
		} else if(state==STATES[2]) {
			if(event instanceof Timer) {
				Timer timer = (Timer)event;
				if(timer.getId().equals(SEND_AGAIN_TIMER)){
					sendAgainTimer.start(scheduler, 100);
					int result;
					try {
						result = operator.doLightReading();
						Message msg = new Message(caller, callee, Message.Reading+result);
						communication.sendRemoteMessage(msg);
						System.out.println("reading sent...");
					} catch (IOException e) {
						e.printStackTrace();
					}
					return EXECUTE_TRANSITION;
				}
			} else if(event instanceof Message) {
				Message msg = (Message)event;
				if(msg.getContent().equals(Message.button2Pressed)){
					Message send = new Message(caller,callee,Message.SenderDisconnect);
					communication.sendRemoteMessage(send);
					operator.blinkLEDs();
					state = STATES[0];
					System.out.println("sender disconnects...");
					return EXECUTE_TRANSITION;
				} else if(msg.getContent().equals(Message.ReceiverDisconnect)) {
					sendAgainTimer.stop();
					operator.blinkLEDs();
					state = STATES[0];
					System.out.println("receiver disconnects...");
					return EXECUTE_TRANSITION;
				} else if(msg.getContent().equals(Message.ICanDisplayReadings)) {
					Message send = new Message(caller,callee,Message.Denied);
					communication.sendRemoteMessage(send);
					System.out.println("request denied...");
					return EXECUTE_TRANSITION;
				}
			}
		}
		return DISCARD_EVENT;
	}
}
