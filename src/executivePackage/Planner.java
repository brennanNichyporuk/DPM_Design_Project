package executivePackage;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import modulePackage.ColorDetection;
import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;
import pilotPackage.Pilot;
import captureFlagPackage.CaptureFlag;
import captureFlagPackage.ClassID;
import basicPackage.IObserver;
import basicPackage.Localization;
import basicPackage.Navigation;
import basicPackage.Odometer;
import basicPackage.OdometerCorrection;

/**
 * This class is responsible for coordinating all responsibilities of the robot.
 * @author brennanNichyporuk
 *
 */
public class Planner extends Thread implements IObserver {
	private EV3MediumRegulatedMotor neck;
	private UltrasonicModule uM;
	private Odometer odo;
	private Navigation nav;
	private Pilot pilot;
	private CaptureFlag cF;

	private boolean active;

	/**
	 * Instantiates several classes.
	 */
	
	private static int flagType;
	
	public Planner(int startingCorner, int opponentHomeZoneLowX, int opponentHomeZoneLowY, int opponentHomeZoneHighX,int opponentHomeZoneHighY, int dropZoneX, int dropZoneY,int flagType) {
		
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		this.neck = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		this.uM = new UltrasonicModule(usSensor, usData, neck);

		this.odo = new Odometer(leftMotor, rightMotor, 20, true);
		this.nav = new Navigation(odo);
		
		
		
		//localizing the robot SET THE STARTING LOCATION TO numbers 1 through 4 depending on 
		//on the starting location
		EV3TouchSensor touch = new EV3TouchSensor(LocalEV3.get().getPort("S4"));
		Localization localizer = new Localization(odo, nav, uM, touch, startingCorner);
		localizer.doLocalization();
		nav.travelTo(0.5*30.48, 0.5*30.48);
		nav.turnTo(90, true);
		
		
		
		//initializing odometry correction after having initialized localization
		SensorModes colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		LineDetection lineDetector = new LineDetection(colorSensor);
		OdometerCorrection odometryCorrecter = new OdometerCorrection(odo, lineDetector);
		odometryCorrecter.start();
		
		//initializing 
		this.flagType = flagType;
		
		this.pilot = new Pilot(this, nav, odo, uM, 0,0, 7, 7);
		pilot.start();
		

	}

	public void run() {
		if (active) {

		}
		else
			this.sleepFor(250);

	}

	/**
	 * Called by observed classes to notify Planner of changes.
	 */

	public void update(ClassID id){
		switch (id) {
		case PILOT:
			SensorModes colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("PORT ADD"));
//			SampleProvider colorValue = colorSensor.getMode("Color ID");
//			float[] colorData = new float[colorValue.sampleSize()];
			ColorDetection cD = new ColorDetection(colorSensor);
			EV3LargeRegulatedMotor robotArmMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
			nav.travelTo(5*30.48, 4*30.48);
			this.cF = new CaptureFlag(this.flagType, nav, odo, uM, cD, robotArmMotor);
			this.cF.start();
		default:
			System.out.println("UNPLANNED ClassID");
		}
	}

	private void sleepFor(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
