package no.ntnu.item.ttm4160.sunspot.runtime;

import no.ntnu.item.ttm4160.sunspot.communication.Message;

public interface IStateMachine {
	
	public static final int 
		EXECUTE_TRANSITION = 0, 
		DISCARD_EVENT = 1,
		TERMINATE_SYSTEM = 2;
	
	public int fire(Scheduler scheduler);
	
	public Queue getEventQueue();
	
	public String getId();
	
	public boolean isTimerDoable(String timerId);
	
}
