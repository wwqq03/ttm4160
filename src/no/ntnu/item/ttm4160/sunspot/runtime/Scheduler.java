package no.ntnu.item.ttm4160.sunspot.runtime;

import java.util.*;
import no.ntnu.item.ttm4160.sunspot.communication.Message;

public class Scheduler{

	/* This simplified scheduler only has one single state machine */
	private Vector stateMachines;
	private int idGenerator;

	public Scheduler() {
		stateMachines = new Vector();
		idGenerator = 0;
	}

	public void run() {
		boolean running = true;
		System.out.println("Starting scheduler...");
		while(running) {
			try {
				for(int i = 0; i < stateMachines.size(); i++){
					// wait for a new event arriving in the queue
					IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
					if(stateMachine.getEventQueue().isEmpty()){
						continue;
					}
					
					// execute a transition
					log("Scheduler: firing state machine " + stateMachine.getId());
					int result = stateMachine.fire(this);
					if(result==IStateMachine.DISCARD_EVENT) {
						log("Discarded Event");
					} else if(result==IStateMachine.TERMINATE_SYSTEM) {
						log("State Machine " + stateMachine.getId() + " is terminated!");
						unRegisterStateMachine(stateMachine);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void registerStateMachine(IStateMachine stateMachine){
			stateMachines.addElement(stateMachine);
			System.out.println("state machine registered " + stateMachine.getId());
	}
	
	public void unRegisterStateMachine(IStateMachine stateMachine){
			stateMachines.removeElement(stateMachine);
	}

	/**
	 * Normal events are enqueued at the end of the queue.
	 * @param event - the name of the event
	 */
	public void addToQueueLast(Object event) {
		if(! (event instanceof Message)){
			return;
		}
		Message msg = (Message)event;
		if(msg.getReceiver() == null && msg.getContent().equals(Message.button2Pressed)){
			//Button 2 pressed, kind of broadcast locally
			for(int i = 0; i < stateMachines.size(); i++){
				IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
				stateMachine.getEventQueue().addLast(msg);
			}
		}
		else if(msg.getReceiverMAC() != null 
				&& msg.getReceiverMAC().equals(Message.BROADCAST_ADDRESS)
				&& msg.getContent().equals(Message.CanYouDisplayMyReadings)){
			//broadcast message
			for(int i = 0; i < stateMachines.size(); i++){
				IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
				if(stateMachine.wishToReceiveBroadcast()){
					stateMachine.getEventQueue().addLast(msg);
				}
			}
		}
		else if(msg.getContent().equals(Message.button1Pressed)){
			//button 1 pressed, kind of broadcast locally
			for(int i = 0; i < stateMachines.size(); i++){
				IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
				stateMachine.getEventQueue().addLast(msg);
			}
		}
		else if(msg.getReceiver() != null && msg.getReceiverStateMachineId() != null){
			//normal messages
			for(int i = 0; i < stateMachines.size(); i++){
				IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
				if(msg.getReceiverStateMachineId().equals(stateMachine.getId())){
					stateMachine.getEventQueue().addLast(msg);
					break;
				}
			}
		}
	}

	/**
	 * Timeouts are added at the first place of the queue.
	 * @param event - the name of the timer
	 */
	public void addToQueueFirst(Object event) {
		String timerId = (String)event;
		
		Timer timer = new Timer(timerId);
		for(int i = 0; i < stateMachines.size(); i++){
			IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
			if(stateMachine.isTimerDoable(timer.getId())){
				stateMachine.getEventQueue().addFirst(timer);
				break;
			}
		}
	}

	private void log(String message) {
		System.out.println(message);
	}
	
	private synchronized String generateNewId(){
		idGenerator++;
		return String.valueOf(idGenerator);
	}
	
	
}
