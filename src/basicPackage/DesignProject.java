package basicPackage;

import pilotPackage.DStarLite;
import pilotPackage.Pilot;
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
import mapPackage.Mapper;
import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class DesignProject {
	
	
	public static void main(String[] args) {
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3MediumRegulatedMotor neck = new 	EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

		Odometer odo = new Odometer(leftMotor, rightMotor, 20, true);
		Navigation navigator = new Navigation(odo);
		
		
		//setting up the ultrasonic sensor for localization
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		UltrasonicModule ultrasonicMod = new UltrasonicModule(usSensor, usData, neck);
		
		DStarLite dS = new DStarLite();
		
		Mapper mapper = new Mapper(odo, ultrasonicMod, dS, 10);
		mapper.start();
		mapper.setActive(true);
		navigator.moveForward();
		
		
		Button.waitForAnyPress();
		System.exit(0);
		
	}
}
