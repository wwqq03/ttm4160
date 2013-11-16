package no.ntnu.item.ttm4160.sunspot;

import java.io.IOException;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ILightSensor;
import com.sun.spot.sensorboard.peripheral.ILightSensorThresholdListener;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.util.Utils;

public class DeviceOperator implements ILightSensorThresholdListener{
	private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();
    private ILightSensor lightSensor = EDemoBoard.getInstance().getLightSensor(); 
    
    public void blinkLEDs(){
		for (int i = 0; i < 8; i++) {
            leds[i].setOff();
		}
		
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < leds.length; j++) {
				leds[0].setOn();
                Utils.sleep(250);
                leds[0].setOff();
                Utils.sleep(750);
			}
		}
	}
    
    public void displayOnLEDs(int result){
    	
    }
    public int doLightReading() throws IOException{
		lightSensor.addILightSensorThresholdListener(this); // register us as a listener
        lightSensor.setThresholds(0, 700);                  // notify if no light measured or if really bright
        lightSensor.enableThresholdEvents(true);
		int result = lightSensor.getValue();  
		return result;
	}
    public void thresholdExceeded(ILightSensor light, int val) {
        String which = (val < 100) ? "Low" : "Bright";
        System.out.println(which + " light level event: light level = " + val);
        Utils.sleep(1000);                                  // wait one second
        lightSensor.enableThresholdEvents(true);            // re-enable notification
    }
    public void thresholdChanged(ILightSensor light, int low, int high) {
        // ignore 
    }
}
