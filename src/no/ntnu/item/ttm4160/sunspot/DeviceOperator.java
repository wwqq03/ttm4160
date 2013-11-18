package no.ntnu.item.ttm4160.sunspot;

import java.io.IOException;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ILightSensor;
import com.sun.spot.sensorboard.peripheral.ILightSensorThresholdListener;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.sensorboard.peripheral.LEDColor;
import com.sun.spot.util.Utils;

public class DeviceOperator implements ILightSensorThresholdListener{
	private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();
    private ILightSensor lightSensor = EDemoBoard.getInstance().getLightSensor();
    private SpotFont font = new SpotFont();
    private int dots[];
    
    public void blinkLEDs(){
		for (int i = 0; i < 8; i++) {
            leds[i].setOff();
		}
		leds[0].setColor(LEDColor.RED); 
		for (int i = 0; i < 3; i++){
			//for (int j = 0; j < leds.length; j++) {
				leds[0].setOn();
                Utils.sleep(100);
                leds[0].setOff();
                Utils.sleep(100);
			//}
		}
	}
    
    public void displayOnLEDs(int result){
    	String displayValue = String.valueOf(result);
    	for( int i = 0; i < displayValue.length(); i++ ){
            displayCharacterForward( displayValue.charAt(i) );
        }
    	
    }
    
    public void displayCharacterForward( char character ){
        try {
            dots = font.getChar(character);
            
            for ( int i = 0; i < dots.length; i++ ){
                bltLEDs( dots[i] );
                // System.out.print(character);
                Thread.sleep(1);
            }
            bltLEDs(0);
            Thread.sleep(1);
            
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void bltLEDs(int ledMap){
        for ( int i = 0; i < leds.length; i++ ) {
        	leds[i].setColor(LEDColor.WHITE);
            leds[i].setOn(((ledMap>>i)&1)==1);
        }
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
