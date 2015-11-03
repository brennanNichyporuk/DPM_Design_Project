package basicPackage;
import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import modulePackage.LineDetection;

public class LightLocalizer {
	/*
	 * Constants that determine what procedures to run upon finding a line.
	 */
	private enum UpdateType {XUpdate, YUpdate, Theta1Set, Theta2Set, Theta3Set, Theta4Set};
	/*
	 *  Four angles read from odometer in light localization routine.
	 */
	private double theta1, theta2, theta3, theta4 = 0;
	/*
	 * Base width of robot measured on the axes.
	 */
	private double BASE_WIDTH = 17.25;
	private Odometer odo;
	private Navigation nav;
	private LineDetection lineDetector;
	/*
	 * The previous value from the light sensor
	 * that the odometer recorded when odometry correction occured.
	 */
	private int lastValue;
	/*
	 * The previous change in the light sensor.
	 */
	private int lastDerivative;
	
	/*
	 * The most negative change in the value coming in from the light sensor.
	 */
	private int lowValue; 
	/*
	 * The most positive change in the value coming in from the light sensor
	 */
	private int highValue;
	/*
	 * Threshold to determine the minimum change in the derivative for a line to be
	 * considered detected.
	 */
	private int minDerivativeChange;
	
	/*
	 * Frequency in ms at which odometry correction routine's will be run.
	 */
	private long CORRECTION_PERIOD = 50;

	/*
	 * @params odo The Odometer instance in charge of detemrining the robot's position
	 * @params nav The Navigation instance in charge of navigating the robot.
	 * @params colorSensor SampleProvider provides samples from the light sensor.
	 * @params colorData Store samples from SampleProvider in this array.
	 */
	public LightLocalizer(Odometer odo, Navigation nav, LineDetection lineDetector) {
		this.odo = odo;
		this.nav = nav;
		this.lineDetector= lineDetector;
	}
	
	/*
	 * Localization routine. Determines the initial position and then instructs
	 * the navigator to travel to (0.0,0.0) and turn to 0.0 degrees
	 */
	public void doLocalization() {
		// drive to location listed in tutorial
		this.initializePosition();

		this.refineOdometer();

		// when done travel to (0,0) and turn to 0 degrees
		nav.travelTo(0.0, 0.0);
		this.nav.turnTo(0.0, true);

	}
	/*
	 * Rotates in a circle and seeks to find four lines on the grid in order to localize.
	 */
	public void refineOdometer() {
		// start rotating and clock all 4 gridlines
		nav.setSpeeds(-Navigation.SLOW, Navigation.SLOW);
		this.findLine(UpdateType.Theta1Set);
		this.findLine(UpdateType.Theta2Set);
		this.findLine(UpdateType.Theta3Set);
		this.findLine(UpdateType.Theta4Set);
		nav.setSpeeds(0, 0);

		// do trig to compute (0,0) and 0 degrees
		double x = BASE_WIDTH*Math.cos(Math.toRadians((theta3+360 - theta1)/2));
		double y = -BASE_WIDTH*Math.cos(Math.toRadians((theta4 - theta2)/2));
		double deltaTheta = 270.0 - theta4 + ((theta3 - theta1)/2);

		deltaTheta += 7;

		double[] position = {x, y, this.correctAngle(deltaTheta + odo.getAng())};
		boolean[] update = {true, true, true};
		odo.setPosition(position, update);
	}
	/*
	 * Routine to determine how far the robot was placed from the edges of the initial square. 
	 * Using in determining the initial position of the robot assuming correct orientation.
	 */
	public void initializePosition() {

		this.nav.turnTo(90.0, true);
		nav.setSpeeds(Navigation.SLOW, Navigation.SLOW);
		this.findLine(UpdateType.YUpdate);
		nav.setSpeeds(0, 0);

		this.nav.turnTo(0.0, true);
		nav.setSpeeds(Navigation.SLOW, Navigation.SLOW);
		this.findLine(UpdateType.XUpdate);
		nav.setSpeeds(0, 0);

		nav.travelTo(-6.0, -6.0);
		nav.turnTo(90, true);
	}
	/*
	 * Helper method to cause the current thread to sleep.
	 */
	public void sleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * update the position based on passed updatetype
	 */
	void update(UpdateType uT) {
		double[] position = new double[3];
		boolean[] update = new boolean[3];

		Sound.beep();

		switch (uT) {
		case XUpdate:
			update[0] = true;
			position[0] = BASE_WIDTH;
			odo.setPosition(position, update);
			update[0] = false;
			this.sleep(250);
			nav.setSpeeds(0, 0);

		case YUpdate:
			update[1] = true;
			position[1] = BASE_WIDTH;
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
	/*
	 * Finds a line based on what update type we are executing.
	 */
	public void findLine(UpdateType uT) {
		//colorSensor.fetchSample(colorData, 0);
		//lastValue = (int)(colorData[0]*100.0);
		lastDerivative = 0;
		this.detectLine(uT);
	}

	/*
	 * This method does not return until a line is found.
	 * When a line is found, this method calls update() -- which updates a variable based on
	 * the UpdateType passed into the function. The method then returns ... at which point it can be called
	 * again.
	 */
	public void detectLine(UpdateType uT) {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			//colorSensor.fetchSample(colorData,0);
			int currentValue = 0;/*(int)(colorData[0]*100.0);*/
			int currentDerivative = currentValue - lastValue;

			// if the derivative is increasing...
			if (currentDerivative >= lastDerivative) {
				// set the lowValue to the minimum value of the derivative (lastDerivative)
				if (currentDerivative < lowValue) {
					lowValue = lastDerivative;
				}
				// similarly... set highValue to the maximum value of the derivative...
				if (currentDerivative > highValue) {
					highValue = currentDerivative;
				}
			} else {

				// if the magnitude of the change in the derivative is greater than 4... we have detected a line
				if (highValue - lowValue > minDerivativeChange) {
					// if we have detected a line ... we run update() which performs 
					this.update(uT);
					lowValue = 0;
					highValue = 0;
					break;
				}

				/*
				 * if the magnitude of the change in the derivative was great enough, then update()
				 * was run and highValue and lowValue was reset... otherwise it was noise and lowValue 
				 * and highValue should be reset anyway...
				 */
				lowValue = 0;
				highValue = 0;
			}

			lastDerivative = currentDerivative;
			lastValue = currentValue;
			// this ensure the odometry correction occurs only once every period

			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	/*
	 * correct the angle if an angle is less than zero
	 * @param angle in degrees
	 */
	double correctAngle(double angle){
		if (angle < 0)
			angle += 360;
		if (angle > 360)
			angle -= 360;
		return angle;
	}


}
