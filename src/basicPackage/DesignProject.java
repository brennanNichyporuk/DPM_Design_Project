package basicPackage;

import java.util.List;

import executivePackage.Planner;
import pilotPackage.DStarLite;
import pilotPackage.Pilot;
import pilotPackage.State;
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

		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3MediumRegulatedMotor neck = new 	EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

		double rightRadius = 2.06;
		double leftRadius = rightRadius*0.988;
		double track = 10.21;
		
		Odometer odo = new Odometer(leftMotor, rightMotor, 20, true);
		//Navigation2 navigator = new Navigation2(odo, leftMotor, rightMotor, 4, 6, leftRadius, rightRadius, track);
		//Navigation navigator = new Navigation(odo);
		
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		UltrasonicModule ultrasonicMod = new UltrasonicModule(usSensor, usData, neck);


		Mapper mapper = new Mapper(odo, ultrasonicMod, null);
		mapper.start();
		mapper.setActive(true);
		//Pilot pilot = new Pilot(null, navigator, odo, ultrasonicMod, 0, 0, 7, 7);
		//pilot.start();

		Button.waitForAnyPress();
		System.exit(0);
	}
}
