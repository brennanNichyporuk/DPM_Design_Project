package basicPackage;

import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class Localization {
	
	/**
	 * How fast the robot is allowed to rotate while localizing
	 */
	public static double ROTATION_SPEED = 30;
	private Odometer odo;
	private Navigation nav;
	private LineDetection lineModule;
	private USLocalizer usLocalizer;
	private LightLocalizer lightLocalizer;
	
	public Localization(Odometer odo, Navigation nav, UltrasonicModule usModule, LineDetection lineModule, USLocalizer.LocalizationType locType) {
		this.odo = odo;
		this.nav = nav;
		this.usLocalizer = new USLocalizer(this.odo, this.nav, usModule,locType);
		this.lightLocalizer = new LightLocalizer(this.odo, this.nav, lineModule);
	}
	
	/**
	 * Executes both ultrasonic and light localization routines in order to get robot's
	 * initial position.
	 */
	public void doLocalization(){
		
	}
	
	
}
