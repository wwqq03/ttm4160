package no.ntnu.item.ttm4160.sunspot;

import no.ntnu.item.ttm4160.sunspot.communication.Communications;
import no.ntnu.item.ttm4160.sunspot.communication.Message;
import no.ntnu.item.ttm4160.sunspot.runtime.Queue;
import no.ntnu.item.ttm4160.sunspot.runtime.Scheduler;
import no.ntnu.item.ttm4160.sunspot.runtime.Timer;
import no.ntnu.item.ttm4160.sunspot.runtime.IStateMachine;

public class CalleeStateMachine implements IStateMachine{
	
	private static final String TIME_OUT_TIMER = "timeOutTimer";
	private static final String STATE_FREE = "free";
	private static final String STATE_BUSY = "busy";
	private static final String STATE_WAIT_APPROVED = "wait_approved";
	private static final String STATE_FINAL = "final";
	private Timer timeOutTimer = new Timer(TIME_OUT_TIMER);
	protected String state = STATE_FREE;
	private String myMAC;
	private DeviceOperator deviceOperator;
	private String caller;
	private Communications communications;
	private Queue eventQueue;
	private String id;
	
	public CalleeStateMachine(String id, String myMAC, DeviceOperator device, Communications communications){
		this.id = id;
		this.myMAC = myMAC;
		this.deviceOperator = device;
		this.communications = communications;
		eventQueue = new Queue();
	}

	public Queue getEventQueue(){
		return this.eventQueue;
	}
	
	public String getId(){
		return this.id;
	}
	
	public int fire(Scheduler scheduler){
		Object event = eventQueue.take();
		
		if(state.equals(STATE_BUSY)){
			//busy state
			if(event instanceof Message){
				Message msg = (Message)event;
				if(msg.getContent().equals(Message.Reading)){
					String reading = msg.getContent().substring(8);
					displayOnLEDS(reading);
					timeOutTimer.restart();
					state = STATE_BUSY;
					return EXECUTE_TRANSITION;
				}
				else if(msg.getContent().equals(Message.SenderDisconnect)){
					timeOutTimer.stop();
					blinkLEDs();
					state = STATE_FREE;
					return TERMINATE_SYSTEM;
				}
				else if(msg.getContent().equals(Message.button2Pressed)){
					timeOutTimer.stop();
					sendReceiverDisconnect(msg);
					blinkLEDs();
					state = STATE_FREE;
					return TERMINATE_SYSTEM;
				}
				else{
					return DISCARD_EVENT;
				}
			}
			else if(event instanceof Timer){
				Timer timer = (Timer)event;
				if(timer.getId().equals(TIME_OUT_TIMER)){
					state = STATE_FREE;
					return TERMINATE_SYSTEM;
				}
			}
			else {
				return DISCARD_EVENT;
			}
		}
		else{
			//not in busy state
			if(event instanceof Message){
				Message msg = (Message)event;
				if(msg.getContent().equals(Message.Reading)){
					sendReceiverDisconnect(msg);
					return EXECUTE_TRANSITION;
				}
			}
			
			if(state.equals(STATE_FREE)){
				//free state
				if(event instanceof Message){
					Message msg = (Message)event;
					if(msg.getContent().equals(Message.CanYouDisplayMyReadings)){
						caller = msg.getSender();
						sendICanDisplayReadings(msg);
						state = STATE_WAIT_APPROVED;
						return EXECUTE_TRANSITION;
					}
					else{
						return DISCARD_EVENT;
					}
				}
				else{
					return DISCARD_EVENT;
				}
			}
			else if(state.equals(STATE_WAIT_APPROVED)){
				//state wait approved
				if(event instanceof Message){
					Message msg = (Message)event;
					if(msg.getContent().equals(Message.Approved)){
						timeOutTimer.start(scheduler, 5000);
						state = STATE_BUSY;
						return EXECUTE_TRANSITION;
					}
					else if(msg.getContent().equals(Message.Denied)){
						state = STATE_FREE;
						return TERMINATE_SYSTEM;
					}
					else if(msg.getContent().equals(Message.CanYouDisplayMyReadings)){
						storeCanYouDisplayMyReadings(msg);
						return EXECUTE_TRANSITION;
					}
					else{
						return DISCARD_EVENT;
					}
				}
			}
			else {
				return DISCARD_EVENT;
			}
		}
		return DISCARD_EVENT;
	}
	
	public boolean isTimerDoable(String timerId){
		return timerId.equals(TIME_OUT_TIMER);
	}

	private void storeCanYouDisplayMyReadings(Message msg) {
		// TODO Auto-generated method stub
		eventQueue.addFirst(msg);
	}

	private void sendICanDisplayReadings(Message msg) {
		// TODO Auto-generated method stub
		Message newMsg = new Message(myMAC + id, caller, Message.ICanDisplayReadings);
		communications.sendRemoteMessage(newMsg);
	}

	private void sendReceiverDisconnect(Message msg) {
		// TODO Auto-generated method stub
		Message newMsg = new Message(myMAC + id, caller, Message.ReceiverDisconnect);
		communications.sendRemoteMessage(newMsg);
	}

	private void blinkLEDs() {
		// TODO Auto-generated method stub
		deviceOperator.blinkLEDs();
	}

	private void displayOnLEDS(String reading) {
		// TODO Auto-generated method stub
		deviceOperator.displayOnLEDs(Integer.valueOf(reading).intValue());
	}
}
