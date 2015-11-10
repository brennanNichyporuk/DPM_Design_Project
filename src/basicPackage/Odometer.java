/* 
 * Odometer.java
 * Group #: 61
 * Names: Fred Glozman (260635610) & Abdel Kader Gaye (260637736) 
 * 
 * This class keeps track of the robots position (in x,y) and orientation (theta)
 */

package basicPackage;

/*
 * File: Odometer.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Class which controls the odometer for the robot
 * 
 * Odometer defines cooridinate system as such...
 * 
 * 					90Deg:pos y-axis
 * 							|
 * 							|
 * 							|
 * 							|
 * 180Deg:neg x-axis------------------0Deg:pos x-axis
 * 							|
 * 							|
 * 							|
 * 							|
 * 					270Deg:neg y-axis
 * 
 * The odometer is initalized to 90 degrees, assuming the robot is facing up the positive y-axis
 * 
 */

import lejos.utility.Timer;
import lejos.utility.TimerListener;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer implements TimerListener {
	/**
	 * Acts as the clock for the odometer. 
	 * Defaults to 20ms if if the timeout interval is given as <= 0.
	 */
	private Timer timer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	/**
	 * Timeout being for the clock in ms.
	 */
	private final int DEFAULT_TIMEOUT_PERIOD = 20;
	/**
	 * Instance variables for car dimensions.
	 */
	private double leftRadius, rightRadius;
	public static double width;

	/**
	 * Current x, y and orientation of the car.
	 */
	private double x, y, theta;
	
	/**
	 * array to store old position and new position estimate from motors.
	 */
	private double[] oldDH, dDH;
	
	/**
	 * @param leftMotor instance of left motor
	 * @param rightMotor instance of right motor
	 * @param interval timer period
	 * @param boolean Determines if the odometer thread will start running immedietly after the constructor has been created.
	 * @param INTERVAL Determines the frequency of the odometer in MS.
	 * @param autoStart Set to true if you want the odometer thread to be called after the constructor is completed.
	 */
	public Odometer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int INTERVAL, boolean autostart) {
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		// default values, modify for your robot
		this.rightRadius = 2.00;
		this.leftRadius = 1.98;
		width = 11.7;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 90.0;
		this.oldDH = new double[2];
		this.dDH = new double[2];

		if (autostart) {
			// if the timeout interval is given as <= 0, default to 20ms timeout 
			this.timer = new Timer((INTERVAL <= 0) ? INTERVAL : DEFAULT_TIMEOUT_PERIOD, this);
			this.timer.start();
		} else
			this.timer = null;
	}
	
	/**
	 *  Starts the TimerListener
	 */
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	
	/**
	 * Stops the TimerListener
	 */
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
	/**
	 * Calculates the displacement and heading.
	 * @param data The array where you want to store displacement and heading.
	 */
	private void getDisplacementAndHeading(double[] data) {
		int leftTacho, rightTacho;
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();

		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI / 360.0;
		data[1] = (rightTacho * rightRadius - leftTacho * leftRadius) / width;
	}
	
	/**
	 * Recompute the odometer values using the displacement and heading changes
	 */
	public void timedOut() {
		this.getDisplacementAndHeading(dDH);
		dDH[0] -= oldDH[0];
		dDH[1] -= oldDH[1];

		// update the position in a critical region
		synchronized (this) {
			theta += dDH[1];
			theta = fixDegAngle(theta);
			x -= dDH[0] * Math.cos(Math.toRadians(theta));
			y -= dDH[0] * Math.sin(Math.toRadians(theta));
			//System.out.println("Value of x "+x);
			//System.out.println("Value of y "+y);
		}

		oldDH[0] += dDH[0];
		oldDH[1] += dDH[1];
	}

	/**
	 * returns the X value
	 * @return double
	 */
	public double getX() {
		synchronized (this) {
			return x;
		}
	}

	/**
	 * returns the Y value
	 * @return double
	 */
	public double getY() {
		synchronized (this) {
			return y;
		}
	}

	/**
	 * return theta value
	 * @return double
	 */
	public double getAng() {
		synchronized (this) {
			return theta;
		}
	}

	/**
	 * set the position of the robot
	 * @param position provide an array with three arguments, x, y and theta values.
	 * @param update determines if the corresponding parameter in position is updaed.
	 */
	public void setPosition(double[] position, boolean[] update) {
		synchronized (this) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	/**
	 * get the current position reported on the odometer
	 * @return double[]
	 */
	public double[] getPosition() {
		synchronized (this) {
			return new double[] { x, y, theta };
		}
	}
	
	// accessors to motors
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}

	/**
	 * Helper method to fix a degree angle (ensure we never pass a negative value)
	 * @param Fix the degree angle to ensure that it is not less than zero.
	 * @return Returns the fixed angle.
	 */
	public static double fixDegAngle(double angle) {
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);

		return angle % 360.0;
	}
	
	/**
	 * Calculate the minimum angle based on the difference between the two angles
	 * @param a The first angle.
	 * @param b The second angle.
	 * @return The minimum travel distance between two angles in degrees.
	 * */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);

		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
}
