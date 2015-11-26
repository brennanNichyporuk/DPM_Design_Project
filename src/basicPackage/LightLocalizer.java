package basicPackage;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import modulePackage.LineDetection;

public class LightLocalizer {
	private enum UpdateType {XUpdate, YUpdate, Theta1Set, Theta2Set, Theta3Set, Theta4Set};
	private double theta1, theta2, theta3, theta4 = 0;
	private double d = 17.25;
	private Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensor;
	private float[] colorData;
	private int lastValue, lastDerivative, lowValue, highValue, minDerivativeChange;
	private long CORRECTION_PERIOD = 50;
	private LineDetection lineDetector;
	
	
	/**
	 * 
	 * @param odo: Odometer to fetch and update position
	 * @param nav
	 * @param lineDetector
	 */
	public LightLocalizer(Odometer odo, Navigation nav, LineDetection lineDetector) {
		System.out.println("IN light Localization");
		this.odo = odo;
		this.nav = nav;
		this.lineDetector = lineDetector;
		lineDetector.colorSensor.fetchSample(lineDetector.colorData,0);
		lineDetector.colorSensor.fetchSample(lineDetector.colorData,0);
		lineDetector.colorSensor.fetchSample(lineDetector.colorData,0);

		this.lastValue = (int)(lineDetector.colorData[0]*100.0);
		this.lastDerivative = 0;

		this.minDerivativeChange = 5;
	}

	public void doLocalization() {
		// drive to location listed in tutorial
		this.initializePosition();

		this.refineOdometer();

		// when done travel to (0,0) and turn to 0 degrees
		nav.travelTo(0.0, 0.0);
		this.nav.turnTo(0.0, true);

	}

	public void refineOdometer() {
		// start rotating and clock all 4 gridlines
		nav.setSpeeds(-Navigation.SLOW, Navigation.SLOW);
		
		while(!this.lineDetector.detectLine()){}
		Sound.beep();
		this.update(UpdateType.Theta1Set);
		
		while(!this.lineDetector.detectLine()){}
		Sound.beep();
		this.update(UpdateType.Theta2Set);
		
		while(!this.lineDetector.detectLine()){}
		Sound.beep();
		this.update(UpdateType.Theta3Set);
		
		
		while(!this.lineDetector.detectLine()){}
		Sound.beep();
		this.update(UpdateType.Theta4Set);
		
		
		nav.setSpeeds(0, 0);

		// do trig to compute (0,0) and 0 degrees
		double x = d*Math.cos(Math.toRadians((theta3+360 - theta1)/2));
		double y = -d*Math.cos(Math.toRadians((theta4 - theta2)/2));
		double deltaTheta = 270.0 - theta4 + ((theta3 - theta1)/2);

		deltaTheta += 7;

		double[] position = {x, y, this.correctAngle(deltaTheta + odo.getAng())};
		boolean[] update = {true, true, true};
		odo.setPosition(position, update);
	}

	public void initializePosition() {

		this.nav.turnTo(90.0, true);
		nav.setSpeeds(Navigation.SLOW, Navigation.SLOW);
		
		this.update(UpdateType.YUpdate);
		nav.setSpeeds(0, 0);

		this.nav.turnTo(0.0, true);
		nav.setSpeeds(Navigation.SLOW, Navigation.SLOW);
		this.update(UpdateType.XUpdate);
		nav.setSpeeds(0, 0);

		nav.travelTo(-6.0, -6.0);
		nav.turnTo(90, true);
	}

	public void sleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void update(UpdateType uT) {
		double[] position = new double[3];
		boolean[] update = new boolean[3];

		Sound.beep();

		switch (uT) {
		case XUpdate:
			update[0] = true;
			position[0] = d;
			odo.setPosition(position, update);
			update[0] = false;
			this.sleep(250);
			nav.setSpeeds(0, 0);

		case YUpdate:
			update[1] = true;
			position[1] = d;
			odo.setPosition(position, update);
			update[1] = false;
			this.sleep(250);
			nav.setSpeeds(0, 0);

		case Theta1Set:
			theta1 = odo.getAng();

		case Theta2Set:
			theta2 = odo.getAng();

		case Theta3Set:
			theta3 = odo.getAng();

		case Theta4Set:
			theta4 = odo.getAng();
		}
	}


	double correctAngle(double angle){
		if (angle < 0)
			angle += 360;
		if (angle > 360)
			angle -= 360;
		return angle;
	}


}
