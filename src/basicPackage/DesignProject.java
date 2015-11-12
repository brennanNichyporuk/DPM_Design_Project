package basicPackage;

import pilotPackage.Pilot;
import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.Button;
import lejos.hardware.Sound;
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
		Odometer2 odo = new Odometer2(leftMotor, rightMotor, 20, true);
		Navigation2 navigator = new Navigation2(odo);
		//LCDInfo lcd = new LCDInfo(odo);
		
		
		//ssetting up the ultrasonic sensor for localization
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		UltrasonicModule ultrasonicMod = new UltrasonicModule(usSensor, usData, neck);
		
		/*
		while (true) {
			System.out.println(ultrasonicMod.getDistance());
		}
		*/
		
		Pilot pilot = new Pilot(null, navigator, odo, ultrasonicMod, 2, 0, 2, 7);
		pilot.start();
		
		Button.waitForAnyPress();
		System.exit(0);
		
		//setting up the color sensor for object identification and localization
		//SensorModes lineSensor = new EV3ColorSensor(LocalEV3.get().getPort("S2"));	
		//LineDetection lineDetector = new LineDetection(lineSensor);
		//System.out.println((int) odo.getX() + "," + (int) odo.getY());
		//navigator.travelTo(0, 30.48 * 5);
		//navigator.turnTo(270, true);
		//Button.waitForAnyPress();
		//System.exit(0);
		
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
