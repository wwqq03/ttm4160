package no.ntnu.item.ttm4160.sunspot.communication;

import no.ntnu.item.ttm4160.sunspot.runtime.Scheduler;

public class CommunicationLayerListenerImp implements ICommunicationLayerListener{
	
	private Scheduler scheduler;
	
	public CommunicationLayerListenerImp(Scheduler scheduler){
		this.scheduler = scheduler;
	}

	public void inputReceived(Message msg){
		scheduler.addToQueueLast(msg);
	}
}
