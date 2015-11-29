package basicPackage;

import java.io.IOException;
import java.util.List;

import executivePackage.Planner;
import pilotPackage.DStarLite;
import pilotPackage.Pilot;
import pilotPackage.State;
import wifi.StartCorner;
import wifi.Transmission;
import wifi.WifiConnection;
import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
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

		/**
		 * PORT CONFIGURATIONS FOR EV3 MODULE:
		 * RIGHT WHEEL MOTOR - A
		 * LEFT WHEEL MOTOR - D
		 * MOTOR FOR CLAW - B
		 * SERVO MOTOR FOR US TURN - C
		 * 
		 * ULTRASONIC SENSOR -> S1
		 * LIGHT ColorDetection SENSOR-> S2
		 * LIGHT LD -> S3
		 * gyroscope-> S4
		 */
	private static final String SERVER_IP = "192.168.10.200";
	private static final int TEAM_NUMBER = 10;
	private static TextLCD LCD = LocalEV3.get().getTextLCD();

	public static void main(String args[]){
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
		EV3MediumRegulatedMotor neck = new 	EV3MediumRegulatedMotor(LocalEV3.get().getPort("C"));

		double rightRadius = 2.06;
		double leftRadius = rightRadius*0.988;
		double track = 10.21;
		
		Odometer odo = new Odometer(leftMotor, rightMotor, 20, true);
		//Navigation2 navigator = new Navigation2(odo, leftMotor, rightMotor, 4, 6, leftRadius, rightRadius, track);
		Navigation nav = new Navigation(odo, leftMotor, rightMotor, 4, 6, leftRadius, rightRadius, track);
		
		SensorModes usSensor = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
		SampleProvider usValue = usSensor.getMode("Distance");
		float[] usData = new float[usValue.sampleSize()];
		UltrasonicModule ultrasonicMod = new UltrasonicModule(usSensor, usData, neck);

		Pilot pilot = new Pilot(null, nav, odo, ultrasonicMod, 0, 0, 6, 6);
		pilot.start();

		Button.waitForAnyPress();
		System.exit(0);
	}
}
