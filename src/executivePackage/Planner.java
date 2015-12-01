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
	public final static int sizeOfBoard = 8;
	private static int[] goalPoint;
	
	public Planner(int startingCorner, int opponentHomeZoneLowX, int opponentHomeZoneLowY, int opponentHomeZoneHighX,int opponentHomeZoneHighY, int dropZoneX, int dropZoneY,int flagType) 
	{
		//recording all parameters.
		Planner.startingCorner = startingCorner;
		Planner.opponentHomeZoneLowX = opponentHomeZoneLowX;
		Planner.opponentHomeZoneLowY = opponentHomeZoneLowY;
		Planner.opponentHomeZoneHighX = opponentHomeZoneHighX;
		Planner.opponentHomeZoneHighY = opponentHomeZoneHighY;
		Planner.flagType = flagType;
		Planner.dropZoneX = dropZoneX;
		Planner.dropZoneY = dropZoneY;
		Planner.goalPoint = chooseGoal();
		//function which sets all parameters to reflect the starting corner.
		interpretCompParams();
		
		//

		
		//initializing motors and various sensors
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
		//odometryCorrecter.CORRECT=false;
		//Localization localizer = new Localization(odo, nav, uM, startingCorner, lineDetector);
		//localizer.doLocalization();
		//gyroCorrecter.setGyro(this.odo.getAng());
		//odometryCorrecter.CORRECT=true;
		//sleepFor(2000);
		//should now be localized.
		nav.travelTo(0.5*30.48, 0.5*30.48,true);
		nav.turnTo(90, true);
		
		//pilot to bottom corner of a tile.
		
		this.pilot = new Pilot(this, nav, odo, uM, 1,1,goalPoint[0]+1,goalPoint[1]);
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
			SampleProvider colorValue = colorSensor.getMode("Color ID");
			float[] colorData = new float[colorValue.sampleSize()];
			ColorDetection cD = new ColorDetection(colorSensor);
			EV3LargeRegulatedMotor robotArmMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
			nav.travelTo(30.48*goalPoint[0],30.48*goalPoint[1],true);
			nav.turnTo(90, true);
			this.cF = new CaptureFlag(flagType, nav, odo, uM, cD, robotArmMotor);
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
	
	
private static void interpretCompParams(){
		
		switch (startingCorner){
		// in first case do nothing, our relative and absolute position is the same.
		case 1: 
			break;
		
		//second case is bottom left corner
		case 2:
			int temp = opponentHomeZoneLowY;
			opponentHomeZoneLowY = (sizeOfBoard-2) - opponentHomeZoneLowX;
			opponentHomeZoneLowX = temp;
			
			temp = opponentHomeZoneHighY;
			opponentHomeZoneHighY = (sizeOfBoard-2) - opponentHomeZoneHighX;
			opponentHomeZoneHighX = temp;
			
			temp = dropZoneY;
			dropZoneY = (sizeOfBoard-2) - dropZoneX;
			dropZoneX = temp;
			
			break;
			
		//third case is top right corner
		case 3:
			opponentHomeZoneLowX = (sizeOfBoard-2) - opponentHomeZoneLowX;
			opponentHomeZoneLowY = (sizeOfBoard-2) - opponentHomeZoneLowY;
			
			opponentHomeZoneHighX = (sizeOfBoard-2) - opponentHomeZoneHighX;
			opponentHomeZoneHighY = (sizeOfBoard-2) - opponentHomeZoneHighY;
			
			dropZoneX = (sizeOfBoard-2) - dropZoneX;
			dropZoneY = (sizeOfBoard-2) - dropZoneY;
			
			break;
		case 4:
			temp = opponentHomeZoneLowX;
			opponentHomeZoneLowX = (sizeOfBoard-2) - opponentHomeZoneLowY;
			opponentHomeZoneLowY = temp;
			
			temp = opponentHomeZoneHighY;
			opponentHomeZoneHighY = opponentHomeZoneHighX;
			opponentHomeZoneHighX = (sizeOfBoard-2) - temp;
			
			temp = dropZoneX;
			dropZoneX = (sizeOfBoard-2) - dropZoneY;
			dropZoneY = temp;
			
			
			break;
		default:
			System.out.println("invalid corner input");
			System.exit(3);
		}
	}
	
	private static int[] convertPoint(int x, int y){
		switch (startingCorner){
		// in first case do nothing, our relative and absolute position is the same.
		case 1: 
			return new int[] {x,y};
		
		//second case is bottom left corner
		case 2:
			int temp = y;
			y = (sizeOfBoard-2) - x;
			x = temp;
			return new int[] {x,y};
			
		//third case is top right corner
		case 3:
			x = (sizeOfBoard-2) - x;
			y = (sizeOfBoard-2) - y;
			return new int[] {x,y};
		case 4:
			temp = x;
			x = (sizeOfBoard-2) - y;
			y = temp;
			return new int[] {x,y};
		default:
			System.out.println("invalid corner input");
			return new int[] {};
		}
	}
	private static int[] chooseGoal(){
		int median = (opponentHomeZoneLowX + opponentHomeZoneLowY)/2;
		if(opponentHomeZoneLowY>(sizeOfBoard/2)){
			return new int[] {median,opponentHomeZoneLowY};
		}
		else{
			return new int[] {median,opponentHomeZoneHighY};
		}
	}
}
