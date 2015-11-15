package basicPackage;

import java.util.Arrays;

import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import modulePackage.UltrasonicModule;

public class Localization {
	
	private Odometer odo;
	private Navigation nav;
	private USLocalizer usLocalizer;
	
	
	private EV3TouchSensor touch;
	float[] touchData;
	
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, EV3TouchSensor touch) {
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,LocalizationType.FALLING_EDGE);
		this.touch=touch;
		touchData = new float[touch.sampleSize()];
	}
	public void doLocalization(){
		//doing ultrasonic localization
		this.usLocalizer.doLocalization();
		this.nav.turnTo(0,true);
		
		//doing localization using touch sensor
		boolean touched = false; 
		nav.setSpeeds(-80,-80);
		
		//while the button is not pressed
		while(!touched){
			touch.fetchSample(touchData, 0);
			if(touchData[0]==1){
				double[] position = {12.5,0,0};
				boolean[] update = {true, false,false};
				this.odo.setPosition(position,update);
				touched = true;
				}
		}
		touched=false;
		
		//get away from wall in case of issues
		nav.moveStraight(10);
		nav.turnTo(90, true);
		nav.setSpeeds(-80,-80);
		//while the button is not pressed
		while(!touched){
			this.touch.fetchSample(touchData, 0);
			if(touchData[0]==1){
				double[] position = {0,12.5,0};
				boolean[] update = {false, true,false};
				this.odo.setPosition(position,update);
				touched = true;
			}
		}
		//we should now be localized!!!
	}
}
