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
	private boolean wishToReceiveBroadcast = true;
	
	public CalleeStateMachine(String id, String myMAC, DeviceOperator device, Communications communications, boolean wishToReceiveBroadcast){
		this.id = id;
		this.myMAC = myMAC;
		this.deviceOperator = device;
		this.communications = communications;
		eventQueue = new Queue();
		this.wishToReceiveBroadcast = wishToReceiveBroadcast;
	}

	public Queue getEventQueue(){
		return this.eventQueue;
	}
	
	public String getId(){
		return this.id;
	}
	
	public int fire(Scheduler scheduler){
		if(eventQueue.isEmpty()){
			return DISCARD_EVENT;
		}
		Object event;
		if(state.equals(STATE_FREE)){
			//If it is free state, just take the first event in the queue
			event = eventQueue.take();
		}
		else{
			//If it is not free state, take the first non-reserved event
			//reserved event is "CanyouDisplayMyReadings" message
			event = takeNonReservedEvent();
		}
		
		
		if(state.equals(STATE_BUSY)){
			//busy state
			if(event instanceof Message){
				Message msg = (Message)event;
				if(msg.getContent().startsWith(Message.Reading)){
					String reading = msg.getContent().substring(8);
					displayOnLEDS(reading);
					timeOutTimer.stop();
					timeOutTimer.start(scheduler, 5000);
					state = STATE_BUSY;
					System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
					return EXECUTE_TRANSITION;
				}
				else if(msg.getContent().equals(Message.SenderDisconnect)){
					timeOutTimer.stop();
					blinkLEDs();
					state = STATE_FREE;
					System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
					return EXECUTE_TRANSITION;
				}
				else if(msg.getContent().equals(Message.button2Pressed)){
					timeOutTimer.stop();
					sendReceiverDisconnect(msg);
					blinkLEDs();
					state = STATE_FREE;
					System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
					return EXECUTE_TRANSITION;
				}
				else{
					return DISCARD_EVENT;
				}
			}
			else if(event instanceof Timer){
				Timer timer = (Timer)event;
				if(timer.getId().equals(TIME_OUT_TIMER)){
					state = STATE_FREE;
					System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
					return EXECUTE_TRANSITION;
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
					System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
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
						System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
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
						System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
						return EXECUTE_TRANSITION;
					}
					else if(msg.getContent().equals(Message.Denied)){
						state = STATE_FREE;
						System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
						return EXECUTE_TRANSITION;
					}
					else if(msg.getContent().equals(Message.CanYouDisplayMyReadings)){
						storeCanYouDisplayMyReadings(msg);
						System.out.println("calleeeeeeeeeeeeeeeeeee: " + state);
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
	
	private Object takeNonReservedEvent() {
		// TODO Auto-generated method stub
		if (eventQueue.isEmpty()){
			return null;
		}
		
		for(int i = 0; i < eventQueue.size(); i++){
			Object event = eventQueue.getElementAt(i);
			if(event instanceof Timer){
				return eventQueue.takeElementAt(i);
			}
			else if(event instanceof Message){
				Message msg = (Message)event;
				if(msg.isReserved()){
					continue;
				}
				else{
					return eventQueue.takeElementAt(i);
				}
			}
			else{
				return null;
			}
		}
		return null;
	}

	public boolean isTimerDoable(String timerId){
		return timerId.equals(TIME_OUT_TIMER);
	}
	
	public boolean wishToReceiveBroadcast(){
		return wishToReceiveBroadcast;
	}
	
	public void setWishToReceiveBroadcast(boolean wish){
		wishToReceiveBroadcast = wish;
	}

	private void storeCanYouDisplayMyReadings(Message msg) {
		// TODO Auto-generated method stub
		msg.setAsReserved();
		eventQueue.addFirst(msg);
	}

	private void sendICanDisplayReadings(Message msg) {
		// TODO Auto-generated method stub
		Message newMsg = new Message(myMAC + ":" + id, caller, Message.ICanDisplayReadings);
		communications.sendRemoteMessage(newMsg);
	}

	private void sendReceiverDisconnect(Message msg) {
		// TODO Auto-generated method stub
		Message newMsg = new Message(myMAC + ":" + id, caller, Message.ReceiverDisconnect);
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
