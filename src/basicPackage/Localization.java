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
	private SensorMode touchMode;
	
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, EV3TouchSensor touch,int startingTileX,int startingTileY) {
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,LocalizationType.FALLING_EDGE);
		this.startTileX = startingTileX;
		this.startTileY = startingTileY;
		this.touch=touch;
		this.touchMode = this.touch.getTouchMode();
		touchData = new float[touch.sampleSize()];
	}
	public void doLocalization(){
		//doing ultrasonic localization
		this.usLocalizer.doLocalization();
		this.usLocalizer.doLocalization();
		//depending on which corner we are in, we need to correct the orientation of the robot to ensure that the
		//angle is consitant regardless of the starting tile. see below for detailed explanation
		this.correctAngle();
		this.correctXAndY();
	}
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
	 * strategy is to touch each wall and set the x or y accordingly.
	 */
	public void correctXAndY(){
		//crash into the wall ON PURPOSE
		if((this.startTileX==1) && (this.startTileY==1)){
			
			this.nav.turnTo(0,true);
			nav.setSpeeds(-80, -80);
			this.touchedWall(true, false, 14.5);
			nav.cm_to_seconds(10);
			nav.turnTo(90, true);
			nav.setSpeeds(-80, -80);
			this.touchedWall(false, true, 14.5);
			
		}
		
		else if((this.startTileX==1) && (this.startTileY==8)){
			
			this.nav.turnTo(0,true);
			nav.setSpeeds(-80, -80);
			this.touchedWall(true, false, 0);
			nav.cm_to_seconds(10);
			nav.turnTo(90, true);
			this.touchedWall(false, true, 0);
			
		}
		
		else if((this.startTileX==8) && (this.startTileY==1)){
			
			this.nav.turnTo(0,true);
			nav.setSpeeds(-80, -80);
			this.touchedWall(true, false, 0);
			nav.cm_to_seconds(10);
			nav.turnTo(90, true);
			this.touchedWall(false, true, 0);
			
		}
		else{
			
			this.nav.turnTo(0,true);
			nav.setSpeeds(-80, -80);
			this.touchedWall(true, false, 0);
			nav.cm_to_seconds(10);
			nav.turnTo(90, true);
			this.touchedWall(false, true, 0);
			
		}
		nav.stop();
	}
	
	public boolean touchedWall(boolean updateX, boolean updateY,double setValue){
		boolean[] update = {updateX, updateY,false};
		boolean touched = false;
		while(!touched){
			this.touchMode.fetchSample(touchData, 0);
			this.touch.fetchSample(touchData, 0);
			if(touchData[0]==1){
				
				double[] position = {0,0,0};
				
				//if updateX is set updateY
				if(updateX){
					position[0]=setValue;
				}
				else{
					position[1] = setValue;
				}
				
				this.odo.setPosition(position,update);
				touched = true;
				}
		}
		return true;
	}
}
