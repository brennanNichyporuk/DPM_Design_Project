package basicPackage;

import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class DesignProject {
	public static void main(String[] args) {
		// Test Comment
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3MediumRegulatedMotor neck = new 	EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));
		Odometer odo = new Odometer(leftMotor, rightMotor, 20, true);
		Navigation navigator = new Navigation(odo);
		//LCDInfo lcd = new LCDInfo(odo);
		
		
		//setting up the ultrasonic sensor for localization
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		UltrasonicModule ultrasonicMod = new UltrasonicModule(usSensor, usData, neck);
		
		//setting up the color sensor for object identification and localization
		SensorModes lineSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));	
		SampleProvider colorValue = lineSensor.getMode("Red");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
		LineDetection lineDetector = new LineDetection(lineSensor, colorData);
		
		
		//initiating localization routine. Includes ultrasonic localization and light sensor localization
		Localization localizer = new Localization(odo, navigator, ultrasonicMod, lineDetector, LocalizationType.FALLING_EDGE);
		localizer.doLocalization();
		navigator.travelTo(30, 30);
		navigator.turnTo(0, true);
		
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
