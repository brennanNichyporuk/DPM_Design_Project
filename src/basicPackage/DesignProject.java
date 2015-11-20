package basicPackage;

import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class DesignProject {
	public static void main(String[] args) {
		// Test Comment
		/**
		 * rightWheel - A
		 * leftWheel - D
		 * clawMotor - B
		 * servo - c
		 * 
		 * US -> S1
		 * LIGHT CD -> S2
		 * LIGHT LD -> S3
		 * TOUCH SENSOR -> S4
		 */
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		//EV3MediumRegulatedMotor neck = new 	EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
		//EV3TouchSensor touch = new EV3TouchSensor(LocalEV3.get().getPort("S4"));
		Odometer odo = new Odometer(leftMotor, rightMotor, 20, true);
		Navigation navigator = new Navigation(odo);
		//LCDInfo lcd = new LCDInfo(odo);
		
		
		//ssetting up the ultrasonic sensor for localization
		//SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		//SampleProvider usValue = usSensor.getMode("Distance");
		//float[] usData = new float[usValue.sampleSize()];
		//UltrasonicModule ultrasonicMod = new UltrasonicModule(usSensor, usData, neck);
		
		//localizing the robot SET THE STARTING LOCATION TO (1,1), (1,8), (8,1) or (8,8) depending
		//on the starting location
		//Localization localizer = new Localization(odo, navigator, ultrasonicMod, touch, 1, 1);
		//localizer.doLocalization();
		
		
		//initializing odometry correction after having initialized localization
		//SensorModes colorSensorL = new EV3ColorSensor(LocalEV3.get().getPort("S3"));
		//LineDetection lineDetector = new LineDetection(colorSensorL);
		//OdometerCorrection odometryCorrecter = new OdometerCorrection(odo, lineDetector);
		//odometryCorrecter.start();
		
		
		navigator.turnTo(0, false);
		navigator.turnTo(120, false);
		navigator.turnTo(240, false);
		navigator.turnTo(0, false);
		navigator.turnTo(120, false);
		navigator.turnTo(240, false);
		navigator.turnTo(0, false);
		navigator.turnTo(120, false);
		navigator.turnTo(240, false);
		navigator.turnTo(0, true);
		//light sensor for odometry correction
		navigator.stop();
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
	}
	public static void sleep(int sleepTime){
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
