package executivePackage;

import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.Sound;
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
import basicPackage.LCDInfo;
import basicPackage.Localization;
import basicPackage.Navigation;
import basicPackage.Odometer;
import basicPackage.OdometerCorrection;

/**
 * This class is responsible for coordinating all responsibilities of the robot.
 * @author brennanNichyporuk
 * 
 * 
 * PORT CONFIGURATIONS FOR EV3 MODULE:
 * RIGHT WHEEL MOTOR - A
 * LEFT WHEEL MOTOR - D
 * MOTOR FOR CLAW - B
 * SERVO MOTOR FOR US TURN - C
 * ULTRASONIC SENSOR -> S1
 * LIGHT ColorDetection SENSOR-> S2
 * LIGHT LD -> S3
 * gyroscope-> S4
 */
public class Planner extends Thread implements IObserver {
	private EV3MediumRegulatedMotor neck;
	private UltrasonicModule uM;
	private Odometer odo;
	private Navigation nav;
	private Pilot pilot;
	private CaptureFlag cF;
	/**
	 * 
	 */
	private boolean active = false;
	
	private static int flagType;
	private static int startingCorner;
	private static int opponentHomeZoneLowX;
	private static int opponentHomeZoneLowY;
	private static int opponentHomeZoneHighX;
	private static int opponentHomeZoneHighY;
	private static int dropZoneX;
	private static int dropZoneY;
	
	public Planner(int startingCorner, int opponentHomeZoneLowX, int opponentHomeZoneLowY, int opponentHomeZoneHighX,int opponentHomeZoneHighY, int dropZoneX, int dropZoneY,int flagType) throws InterruptedException 
	{
		//recording all parameters.
		startingCorner = startingCorner;
		opponentHomeZoneLowX = opponentHomeZoneLowX;
		opponentHomeZoneLowY = opponentHomeZoneLowY;
		opponentHomeZoneHighX = opponentHomeZoneHighX;
		opponentHomeZoneHighY = opponentHomeZoneHighY;
		flagType = flagType;
		dropZoneX = dropZoneX;
		dropZoneY = dropZoneY;
		
		//initializing motors and various sensors for the trial run.
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3GyroSensor gyro = new EV3GyroSensor(SensorPort.S4);
		
		//ensure that travelTo and turnTo are not called during localization routines, otherwise gyro will correct angle.
		GyroCorrection gyroCorrecter = new GyroCorrection(gyro);
		SensorModes colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		this.odo = new Odometer(leftMotor, rightMotor, 20, true);
		LineDetection lineDetector = new LineDetection(colorSensor);
		OdometerCorrection odometryCorrecter = new OdometerCorrection(odo, lineDetector);
		odometryCorrecter.start();
		this.nav = new Navigation(odo, leftMotor, rightMotor, 4, 6,this.odo.leftRadius, this.odo.rightRadius, this.odo.width,gyroCorrecter,odometryCorrecter);
		
		//displays current location on odometer if not commented out.
		//LCDInfo lcd = new LCDInfo(odo);
		//lcd.timedOut();
		
		
		//intializing the ultrasonic sensor module.
		this.neck = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		this.uM = new UltrasonicModule(usSensor, usData, neck);
	
		// ensuring that odometryCorrection does not occur during localization
		odometryCorrecter.CORRECT=false;
		Localization localizer = new Localization(odo, nav, uM, startingCorner, lineDetector);
		localizer.doLocalization();
		this.sleepFor(50000);
		//odometryCorrecter.CORRECT=true;
		
		//will have to update this based on starting position.
		//nav.travelTo(1.5*30.48, 1.5*30.48);
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
		this.sleepFor(5999999);
		
		this.pilot = new Pilot(this, nav, odo, uM, 1,1, 6,6);
		pilot.start();
		

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
	 * @param takes as input a class ID which determines which routine should be executed.
	 */
	public void update(ClassID id){
		switch (id) {
		case PILOT:
			SensorModes colorSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));
//			SampleProvider colorValue = colorSensor.getMode("Color ID");
//			float[] colorData = new float[colorValue.sampleSize()];
			ColorDetection cD = new ColorDetection(colorSensor);
			EV3LargeRegulatedMotor robotArmMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
			nav.travelTo((this.opponentHomeZoneLowX+1)*30.48, this.opponentHomeZoneLowY*30.48);
			nav.turnTo(90, true);
			this.cF = new CaptureFlag(this.flagType, nav, odo, uM, cD, robotArmMotor);
			this.cF.start();
		default:
			System.out.println("UNPLANNED ClassID");
		}
	}
	/**
	 * Will cause the planner thread to sleep for a certain amount of time.
	 * @param t time in milliseconds that is required to sleep.
	 */
	private void sleepFor(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
