package basicPackage;

import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class Localization {
	
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
	public void doLocalization(){
		//doing ultrasonic localization
		this.usLocalizer.doLocalization();
		nav.turnTo(0, true);
		//doing light localization as well
		//this.lightLocalizer.doLocalization();
	}
	
	
}
