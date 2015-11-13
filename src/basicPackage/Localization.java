package basicPackage;

import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import modulePackage.UltrasonicModule;

public class Localization {
	
	private Odometer odo;
	private Navigation nav;
	private USLocalizer usLocalizer;
	
	private EV3TouchSensor touch;
	private SensorMode touchMode;
	float[] touchData;
	
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, EV3TouchSensor touch, USLocalizer.LocalizationType locType) {
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,locType);
		this.touch = touch;
		this.touchMode = this.touch.getTouchMode();
	}
	public void doLocalization(){
		//doing ultrasonic localization
		this.usLocalizer.doLocalization();
		boolean touched = false; 
		
		//turn to 0 degrees and move backwards
		nav.turnTo(0, true);
		nav.setSpeeds(-30,-30);
		
		//while the button is not pressed
		while(!touched){
			this.touchMode.fetchSample(touchData, 0);
			if(touchData[0]==1){
				double[] position = {0,0,0};
				boolean[] update = {true, false,false};
				this.odo.setPosition(position,update);
				touched = true;
				}
		}
		touched=false;
		
		//get away from wall in case of issues
		nav.moveStraight(10);
		nav.turnTo(90, true);
		nav.setSpeeds(-30,-30);
		//while the button is not pressed
		while(!touched){
			this.touchMode.fetchSample(touchData, 0);
			if(touchData[0]==1){
				//update these values to reflect the distance from the center of the axel of the wheels to the car.
				double[] position = {0,0,0};
				boolean[] update = {false, true,false};
				this.odo.setPosition(position,update);
				touched = true;
			}
		}
		//we should now be localized!!!
	}
}
