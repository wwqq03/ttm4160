package no.ntnu.item.ttm4160.sunspot.runtime;

import java.util.Vector;

public class StringQueue {
	
	private Vector queue;
	
	public StringQueue(){
		queue = new Vector();
	}
	
	public String take(){
		if(queue == null || queue.isEmpty()){
			return null;
		}
		Object headElement = queue.firstElement();
		queue.removeElementAt(0);
		return (String)headElement;
	}
	
	public void addLast(String ob){
		if(queue == null){
			return;
		}
		queue.addElement(ob);
	}
	
	public void addFirst(String ob){
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
