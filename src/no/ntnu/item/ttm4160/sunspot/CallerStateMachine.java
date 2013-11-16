package no.ntnu.item.ttm4160.sunspot;

import java.io.IOException;

import com.sun.spot.peripheral.Spot;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ILightSensor;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;

import no.ntnu.item.ttm4160.sunspot.runtime.IStateMachine;
import no.ntnu.item.ttm4160.sunspot.runtime.Scheduler;
import no.ntnu.item.ttm4160.sunspot.runtime.Timer;
import no.ntnu.item.ttm4160.sunspot.communication.Message;
import no.ntnu.item.ttm4160.sunspot.communication.Communications;

public class CallerStateMachine implements IStateMachine {
	
	private static final String BUTTON_1_PRESSED = "Broadcast Button", BUTTON_2_PRESSED = "Disconnect button";
	private static final String ICANDISPLAYREADINGS = "i can dispaly readings";
	private static final String RECEIVERDISCONNECT = "receiver disconnect";
	private static final String GIVEUP_TIMER = "giveUpTimer", SEND_AGAIN_TIMER = "sendAgainTimer";
	
	public static final String[] EVENTS = {BUTTON_1_PRESSED, BUTTON_2_PRESSED, ICANDISPLAYREADINGS};
	
	private static final String[] STATES = {"READY", "WAIT_RESPONSE", "SENDING"};
	private Timer giveUpTimer = new Timer(GIVEUP_TIMER);
	private Timer sendAgainTimer = new Timer(SEND_AGAIN_TIMER);
	
	protected String state = STATES[0];
	
	private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();
    private ILightSensor lightSensor = EDemoBoard.getInstance().getLightSensor();   
	
	private String sender = "", receiver = "", content = "";
	Message msg = new Message(sender,receiver,content);
	Communications communication = new Communications(sender);
	
	DeviceOperator operator = new DeviceOperator();
	 
	

	public int fire(Object event, Scheduler scheduler) {
		if(state==STATES[0]) {
			if(event.equals(BUTTON_1_PRESSED)) {
				giveUpTimer.start(scheduler, 500);
				receiver = msg.BROADCAST_ADDRESS;
				content = msg.CanYouDisplayMyReadings;
				communication.sendRemoteMessage(msg);
				state = STATES[1];
				return EXECUTE_TRANSITION;
			} else if(event.equals(ICANDISPLAYREADINGS)){
				content = msg.Denied;
				communication.sendRemoteMessage(msg);
				return EXECUTE_TRANSITION;
			}
		} else if(state==STATES[1]) {
			if(event.equals(ICANDISPLAYREADINGS)) {
				content = msg.Approved;
				communication.sendRemoteMessage(msg);
				sendAgainTimer.start(scheduler, 100);
				state = STATES[2];
				return EXECUTE_TRANSITION;
			} else if(event.equals(giveUpTimer)){
				operator.blinkLEDs();
				state = STATES[0];
				return EXECUTE_TRANSITION;
			}
		} else if(state==STATES[2]) {
			if(event.equals(sendAgainTimer)) {
				sendAgainTimer.start(scheduler, 100);
				int result;
				try {
					result = operator.doLightReading();
					content = msg.Reading+result;
					communication.sendRemoteMessage(msg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return EXECUTE_TRANSITION;
			} else if(event.equals(BUTTON_2_PRESSED)) {
				content = msg.SenderDisconnect;
				communication.sendRemoteMessage(msg);
				operator.blinkLEDs();
				state = STATES[0];
				return EXECUTE_TRANSITION;
			} else if(event.equals(RECEIVERDISCONNECT)) {
				sendAgainTimer.stop();
				operator.blinkLEDs();
				state = STATES[0];
				return EXECUTE_TRANSITION;
			} else if(event.equals(ICANDISPLAYREADINGS)) {
				content = msg.Denied;
				communication.sendRemoteMessage(msg);
				state = STATES[0];
				return EXECUTE_TRANSITION;
			}
		}
		return DISCARD_EVENT;
	}
}
