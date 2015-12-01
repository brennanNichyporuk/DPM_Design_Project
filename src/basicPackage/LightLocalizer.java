package basicPackage;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import modulePackage.LineDetection;

public class LightLocalizer {
	private enum UpdateType {XUpdate, YUpdate, Theta1Set, Theta2Set, Theta3Set, Theta4Set};
	private double theta1, theta2, theta3, theta4 = 0;
	private static double d = 13.0;
	private Odometer odo;
	private Navigation nav;
	private int lastValue, lastDerivative, lowValue, highValue, minDerivativeChange;
	private LineDetection lineDetector;
	public LightLocalizer(Odometer odo, Navigation nav, LineDetection lineDetector) {
		this.odo = odo;
		this.nav = nav;
		this.lineDetector = lineDetector;
	}

	/**
	 * executes the light localization routine.
	 */
	public void doLocalization() {
		// drive to location listed in tutorial
		//System.out.println("initializePosition");
		//this.initializePosition();
		this.refineOdometer();
	}
	/**
	 * executes the circular motion for light localization in order to determine exact position and angle.
	 */
	public void refineOdometer(){
		// start rotating and clock all 4 gridlines
		nav.setSpeeds(-Navigation.SLOW, Navigation.SLOW);
		for (int i = 0; i < 5; i++)
			this.lineDetector.detectLine();
		
		while(!this.lineDetector.detectLine())
		{
			this.sleep(50);
		}
		
		
		this.update(UpdateType.Theta1Set);
		
		while(!this.lineDetector.detectLine())
		{
			this.sleep(50);
		}
		
		this.update(UpdateType.Theta2Set);
		
		while(!this.lineDetector.detectLine())
		{
			this.sleep(50);
		}
		
		
		
		this.update(UpdateType.Theta3Set);

		
		while(!this.lineDetector.detectLine())
		{
			this.sleep(50);
		}
		double odoAngle = this.odo.getAng();
		nav.setSpeeds(0, 0);
		this.update(UpdateType.Theta4Set);
		
		
		

		// do trig to compute (0,0) and 0 degrees
		double x = d*Math.cos(Math.toRadians((theta3+360 - theta1)/2));
		double y = -d*Math.cos(Math.toRadians((theta4 - theta2)/2));
		double deltaTheta = 270.0 - theta4 + ((theta3 - theta1)/2);

		double overTurn = 0.0;
//		
		double[] position = {x, y, this.correctAngle(deltaTheta + odoAngle+overTurn)};
		boolean[] update = {true, true, true};
		odo.setPosition(position, update);
		this.sleep(5000);
	}
	/*
	 * initialize the position by reading the lines on x and y axis.
	 */
	
	public void initializePosition() {

		this.nav.turnTo(90.0, true);
		nav.setSpeeds(Navigation.SLOW, Navigation.SLOW);
		
		this.update(UpdateType.YUpdate);
		nav.setSpeeds(0, 0);
		
		this.sleep(500);

		this.nav.turnTo(0.0, true);
		
		this.sleep(500);
		
		nav.setSpeeds(Navigation.SLOW, Navigation.SLOW);
		
		this.sleep(500);
		
		this.update(UpdateType.XUpdate);
		nav.setSpeeds(0, 0);
		
		this.sleep(500);

		nav.travelTo(6.0, 6.0,false);
		nav.turnTo(90, true);
	}

	private void sleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * update the odometer based on an enumerable type for which odometer update is required.
	 * @param uT see enumerable types for different updates avaliable.
	 */
	void update(UpdateType uT) {
		double[] position = new double[3];
		boolean[] update = new boolean[3];

		//Sound.beep();

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