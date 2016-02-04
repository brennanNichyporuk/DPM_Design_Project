package basicPackage;

import java.util.Arrays;

import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.Sound;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.SensorMode;
import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class Localization {
	
	private Odometer odo;
	private Navigation nav;
	private USLocalizer usLocalizer;
	private LightLocalizer lightLocalizer;
	private LineDetection lineDetector;
	public static int startTileX;
	public static int startTileY;
	/**
	 * what is the maximum field size? Helpful in determining the location based on starting corner.
	 */
	private static int FIELDSIZE = 8;
	
	/**
	 * constructor for 
	 * @param odo
	 * @param nav
	 * @param usModule
	 * @param startingCorner, which corner are we starting in? Must update starting position based on starting corner.
	 * @param lineDetector
	 */
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, int startingCorner, LineDetection lineDetector) {
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,LocalizationType.FALLING_EDGE);
		
		this.lightLocalizer = new LightLocalizer(odo, nav, lineDetector);
		this.lineDetector = lineDetector;
}
	/*
	 * directs the uslocalizer and lightlocalizer to execute localization
	 */
	public void doLocalization(){
		this.usLocalizer.doLocalization();
		
		nav.turnToContinous(45, true);
		nav.moveForward();
		sleep(1500);
		this.lightLocalizer.doLocalization();
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
