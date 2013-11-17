package no.ntnu.item.ttm4160.sunspot.runtime;

import java.util.*;

import no.ntnu.item.ttm4160.sunspot.CalleeStateMachine;
import no.ntnu.item.ttm4160.sunspot.DeviceOperator;
import no.ntnu.item.ttm4160.sunspot.communication.Communications;
import no.ntnu.item.ttm4160.sunspot.communication.Message;

public class Scheduler{

	/* This simplified scheduler only has one single state machine */
	private Vector stateMachines;
	private int idGenerator;
	private String myMAC;
	private DeviceOperator myDeviceOperator;
	private Communications myCommunications;

	public Scheduler(String myMAC, DeviceOperator myDeviceOperator, Communications myCommunications) {
		this.myMAC = myMAC;
		this.myDeviceOperator = myDeviceOperator;
		this.myCommunications = myCommunications;
		stateMachines = new Vector();
		idGenerator = 1;
	}

	public void run() {
		boolean running = true;
		while(running) {
			try {
				int i = 0;
				while(i < stateMachines.size()){
					// wait for a new event arriving in the queue
					IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
					synchronized (stateMachine.getEventQueue()) {
						if(stateMachine.getEventQueue().isEmpty()){
							continue;
						}
					}
					
					// execute a transition
					log("Scheduler: firing state machine " + stateMachine.getId());
					int result = stateMachine.fire(this);
					if(result==IStateMachine.DISCARD_EVENT) {
						log("Discarded Event: ");
					} else if(result==IStateMachine.TERMINATE_SYSTEM) {
						log("Terminating System... Good bye!");
						running = false;
					}
					
					i++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	void registerStateMachine(IStateMachine stateMachine){
		synchronized (stateMachines) {
			stateMachines.addElement(stateMachine);
		}
	}
	
	void unRegisterStateMachine(IStateMachine stateMachine){
		synchronized (stateMachines) {
			stateMachines.removeElement(stateMachine);
		}
	}

	/**
	 * Normal events are enqueued at the end of the queue.
	 * @param event - the name of the event
	 */
	void addToQueueLast(Object event) {
		if(! (event instanceof Message)){
			return;
		}
		Message msg = (Message)event;
		if(msg.getReceiver() == null && msg.getContent().equals(Message.button2Pressed)){
			//Button 2 pressed
			for(int i = 0; i < stateMachines.size(); i++){
				IStateMachine stateMachine = (IStateMachine)stateMachines.elementAt(i);
				stateMachine.getEventQueue().addLast(msg);
			}
		}
		else if(msg.getReceiverMAC() != null 
				&& msg.getReceiverMAC().equals(Message.BROADCAST_ADDRESS)
				&& msg.getContent().equals(Message.CanYouDisplayMyReadings)){
			//Get broadcast message
			String newId = generateNewId();
			IStateMachine stateMachine = new CalleeStateMachine(newId, myMAC, myDeviceOperator, myCommunications);
			synchronized (stateMachines) {
				stateMachines.addElement(stateMachine);
			}
			stateMachine.getEventQueue().addLast(msg);
		}
		else if(msg.getReceiverStateMachineId() != null && msg.getReceiverStateMachineId() != null){
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
	void addToQueueFirst(Object event) {
		if(! (event instanceof Timer)){
			return;
		}
		
		Timer timer = (Timer)event;
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
