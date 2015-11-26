package executivePackage;

import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
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
import basicPackage.GyroCorrection;
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

	private boolean active = false;

	/**
	 * Instantiates several classes.
	 */
	
	private static int flagType;
	private static int opponentHomeZoneLowX;
	private static int opponentHomeZoneLowY;
	private static int opponentHomeZoneHighX;
	private static int opponentHomeZoneHighY;
	
	public Planner(int startingCorner, int opponentHomeZoneLowX, int opponentHomeZoneLowY, int opponentHomeZoneHighX,int opponentHomeZoneHighY, int dropZoneX, int dropZoneY,int flagType) {
		this.opponentHomeZoneLowX = opponentHomeZoneLowX;
		this.opponentHomeZoneLowY = opponentHomeZoneLowY;
		this.opponentHomeZoneHighX = opponentHomeZoneHighX;
		this.opponentHomeZoneHighY = opponentHomeZoneHighY;
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		this.odo = new Odometer(leftMotor, rightMotor, 20, true);
		this.nav = new Navigation(odo, leftMotor, rightMotor, 4, 6,this.odo.leftRadius, this.odo.rightRadius, this.odo.width);
		
		
		

		
		this.neck = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		this.uM = new UltrasonicModule(usSensor, usData, neck);
		
		SensorModes colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		LineDetection lineDetector = new LineDetection(colorSensor);
		Localization localizer = new Localization(odo, nav, uM, startingCorner, lineDetector);
		localizer.doLocalization();
		
		
		//OdometerCorrection odometryCorrecter = new OdometerCorrection(odo, lineDetector);
		//odometryCorrecter.start();
		//EV3GyroSensor gyro = new EV3GyroSensor(SensorPort.S4);
		//Thread gyroCorrecter = new Thread(new GyroCorrection(gyro));
		//gyroCorrecter.start();
		
		
		//initializing 
//		this.flagType = flagType;
		
//		this.pilot = new Pilot(this, nav, odo, uM, 0,0, opponentHomeZoneLowX, opponentHomeZoneLowY);
//		pilot.start();
		

	}

	public void run() 
	{
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
			SampleProvider colorValue = colorSensor.getMode("Color ID");
			float[] colorData = new float[colorValue.sampleSize()];
			ColorDetection cD = new ColorDetection(colorValue, colorData);
			EV3LargeRegulatedMotor robotArmMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
			nav.travelTo((this.opponentHomeZoneLowX+1)*30.48, this.opponentHomeZoneLowY*30.48);
			nav.turnTo(90, true);
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
