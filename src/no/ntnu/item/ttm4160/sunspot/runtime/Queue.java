package no.ntnu.item.ttm4160.sunspot.runtime;

import java.util.Vector;

import no.ntnu.item.ttm4160.sunspot.communication.Message;

public class Queue {
	
	private Vector queue;
	
	public Queue(){
		queue = new Vector();
	}
	
	public Object take(){
		if(queue == null || queue.isEmpty()){
			return null;
		}
		Object headElement = queue.firstElement();
		queue.removeElementAt(0);
		return headElement;
	}
	
	public void addLast(Object ob){
		if(queue == null){
			return;
		}
		queue.addElement(ob);
	}
	
	public void addFirst(Object ob){
		if(queue == null){
			return;
		}
		queue.insertElementAt(ob, 0);
	}
	
	public boolean isEmpty(){
		if(queue == null){
			return true;
		}
		return queue.isEmpty();
	}

}
