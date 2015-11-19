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
	private int startTileX;
	private int startTileY;
	
	
	private EV3TouchSensor touch;
	float[] touchData;
	
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, EV3TouchSensor touch,int startingTileX,int startingTileY) {
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,LocalizationType.FALLING_EDGE);
		this.startTileX = startingTileX;
		this.startTileY = startingTileY;
		this.touch=touch;
		touchData = new float[touch.sampleSize()];
	}
	public void doLocalization(){
		//doing ultrasonic localization
		this.usLocalizer.doLocalization();
		
		//depending on which corner we are in, we need to correct the orientation of the robot to ensure that the
		//angle is consitant regardless of the starting tile. see below for detailed explanation
		this.correctAngle();
		this.nav.turnTo(0,true);
		
	}
	
	
	//using tile (1,1) as a basis the orientation is as follows:
	/*
	 * Odometer defines cooridinate system as such...
	 * 
	 * 					90Deg:pos y-axis
	 * 							|
	 * 							|
	 * 							|
	 * 							|
	 * 180Deg:neg x-axis------------------0Deg:pos x-axis
	 * 							|
	 * 							|
	 * 							|
	 * 							|
	 * 					270Deg:neg y-axis
	 * 
	 * The odometer is initalized to 90 degrees, assuming the robot is facing up the positive y-axis
	 * 
	 * if we start at the (8,1) corner then we are actually at 270 degrees. (set the orientation to 270 degrees)
	 * if we start at (8,8) we are actually at an orietnation of 90 degrees after localization
	 * if we start at (1,1) we are properly at 0 degrees
	 * if we start at (1,8) we are actually at 180 degrees. 
	 */
	public void correctAngle(){
		double[] position = {0,0,0};
		boolean[] update = {false, false,true};
		if((this.startTileX==1) && (this.startTileY==1) ){
			position[2]=this.odo.getAng();
			this.odo.setPosition(position, update);
		}
		else if((this.startTileX==8) && (this.startTileY==1) ){
			position[2]=this.odo.getAng()+90;
			this.odo.setPosition(position, update);
		}
		else if( (this.startTileX==1) && (this.startTileY==8) ){
			position[2]=this.odo.getAng()-90;
			this.odo.setPosition(position, update);
		}
		else{
			position[2]=this.odo.getAng()+180;
			this.odo.setPosition(position, update);
		}
	}
	/**
	 * depending on starting orientation, we need to orient in different ways. 
	 * strategy is to crash into the wall at a 45 degree angle ( set this angle as 45 degrees, and the position from the origin
	 */
	public void orient(double wallAngle){
		//crash into the wall ON PURPOSE
		nav.setSpeeds(-80, -80);
		
	}
}
