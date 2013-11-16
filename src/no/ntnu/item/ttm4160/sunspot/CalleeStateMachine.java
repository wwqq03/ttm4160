package no.ntnu.item.ttm4160.sunspot;

import no.ntnu.item.ttm4160.sunspot.communication.Message;
import no.ntnu.item.ttm4160.sunspot.runtime.Scheduler;
import no.ntnu.item.ttm4160.sunspot.runtime.Timer;
import no.ntnu.item.ttm4160.sunspot.runtime.IStateMachine;

public class CalleeStateMachine implements IStateMachine{
	
	private static final String TIME_OUT_TIMER = "timeOutTimer";
	private static final String STATE_FREE = "free";
	private static final String STATE_BUSY = "busy";
	private static final String STATE_WAIT_APPROVED = "wait_approved";
	private static final String STATE_FINAL = "final";
	private Timer timeOutTime = new Timer(TIME_OUT_TIMER);
	protected String state = STATE_FREE;
	
	public int fire(Object event, Scheduler scheduler){
		if(state.equals(STATE_BUSY)){
			if(event.equals(Message.Reading)){
				
			}
		}
	}
}
