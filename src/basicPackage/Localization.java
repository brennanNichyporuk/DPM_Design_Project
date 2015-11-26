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
	private static int FIELDSIZE = 8;
	
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, int startingCorner, LineDetection lineDetector) {
		System.out.println("Localization Initilaized");
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,LocalizationType.FALLING_EDGE);
		
		this.lightLocalizer = new LightLocalizer(odo, nav, lineDetector);
		this.lineDetector = lineDetector;
}
	public void doLocalization() throws InterruptedException{
		System.out.println("doing USLocalization");
		this.usLocalizer.doLocalization();
		Thread.sleep(500);
		System.out.println("doing Light Localization");
		this.lightLocalizer.doLocalization();
	}
	/**
	 * depending on starting orientation, we need to orient in different ways. 
	 * strategy is to touch each wall and set the x or y accordingly.
	 */
	
	public static void sleep(int sleepTime){
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
