/*
 * Copyright (c) 2006 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package no.ntnu.item.ttm4160.sunspot;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import no.ntnu.item.ttm4160.sunspot.communication.CommunicationLayerListenerImp;
import no.ntnu.item.ttm4160.sunspot.communication.Communications;
import no.ntnu.item.ttm4160.sunspot.communication.ICommunicationLayerListener;
import no.ntnu.item.ttm4160.sunspot.communication.Message;
import no.ntnu.item.ttm4160.sunspot.runtime.IStateMachine;
import no.ntnu.item.ttm4160.sunspot.runtime.Scheduler;

import com.sun.spot.peripheral.Spot;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ISwitch;
import com.sun.spot.sensorboard.peripheral.ISwitchListener;
import com.sun.spot.sensorboard.peripheral.LightSensor;
import com.sun.spot.util.BootloaderListener;
import com.sun.spot.util.IEEEAddress;

/*
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class SunSpotApplication extends MIDlet implements ISwitchListener{
	
	Scheduler scheduler;
	ISwitch button1, button2;;
	EDemoBoard eDemo;
	ISwitchListener listener;
	String myMAC;
	DeviceOperator myDeviceOperator;
	Communications myCommunications;
	private int idGenerator = 0;
	
	
	public void subscribeSpot(){
		button1 = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW1];  
        button2 = EDemoBoard.getInstance().getSwitches()[EDemoBoard.SW2];
        button1.addISwitchListener(this);
        button2.addISwitchListener(this);
	}
	
	public void switchReleased(ISwitch sw) {
        //int switchNum = (sw == sw1) ? 1 : 2;
        //System.out.println("Switch " + switchNum + " opened.");
    }
	
	public void switchPressed(ISwitch sw) {
        int switchNum = (sw == button1) ? 1 : 2;
        System.out.println("Switch " + switchNum + " pressed.");
        Message msg;
        if(switchNum == 1){
        	msg = new Message(myMAC,Message.BROADCAST_ADDRESS,Message.button1Pressed);
        	scheduler.addToQueueLast(msg);
        	System.out.println("add button 1 event to queue...");
        }else if(switchNum ==2){
        	msg = new Message(myMAC,null,Message.button2Pressed);
        	scheduler.addToQueueLast(msg);
        }
    }
	
    protected void startApp() throws MIDletStateChangeException {
    	
        new BootloaderListener().start();   // monitor the USB (if connected) and recognize commands from host
        // So you don't have to reset SPOT to deploy new code on it.

        /*
         * Instantiate the scheduler and the state machines, then start the scheduler.
         */
        myMAC = new IEEEAddress(Spot.getInstance().getRadioPolicyManager().getIEEEAddress()).asDottedHex();
        
        myDeviceOperator = new DeviceOperator();
        myCommunications = new Communications(myMAC);
        
        scheduler = new Scheduler();
        scheduler.registerStateMachine(
        		new CallerStateMachine(
        				generateNewStateMachineId(),
        				myMAC,
        				myDeviceOperator,
        				myCommunications,
        				false
        		)
        );
        scheduler.registerStateMachine(
        		new CalleeStateMachine(
        				generateNewStateMachineId(),
        				myMAC,
        				myDeviceOperator,
        				myCommunications,
        				true
        		)
        );
        ICommunicationLayerListener communicationListener = new CommunicationLayerListenerImp(scheduler);
        myCommunications.registerListener(communicationListener);
        subscribeSpot();
        scheduler.run();
    }
    
    
    
   
    
    
    
    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }
    
    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     * 
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    	
    	
    }
    
    
    private String generateNewStateMachineId(){
    	idGenerator ++;
    	return String.valueOf(idGenerator);
    }

    
}
