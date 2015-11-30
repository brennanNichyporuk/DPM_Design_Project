package basicPackage;
/* 
 * Odometer.java
 * Group #: 61
 * Names: Fred Glozman (260635610) & Abdel Kader Gaye (260637736) 
 * 
 * This class keeps track of the robots position (in x,y) and orientation (theta)
 */


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

	private Timer timer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private final int DEFAULT_TIMEOUT_PERIOD = 20;
	public double leftRadius, rightRadius, width;
	private double x, y, theta;
	private double[] oldDH, dDH;
	/**
	 * 
	 * @param leftMotor
	 * @param rightMotor
	 * @param INTERVAL how often the odometer should poll the wheels.
	 * @param autostart set to true to have the odometer thread start immedietly on construction.
	 */
	public Odometer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int INTERVAL, boolean autostart) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		// default values, modify for your robot
		this.rightRadius = 2.06;
		this.leftRadius = rightRadius*0.988;
		this.width =10.21;
		this.x = 30.48;
		this.y = 30.48;
		this.theta = 90;
		this.oldDH = new double[2];
		this.dDH = new double[2];
		if (autostart) {
			// if the timeout interval is given as <= 0, default to 20ms timeout 
			this.timer = new Timer((INTERVAL <= 0) ? INTERVAL : DEFAULT_TIMEOUT_PERIOD, this);
			this.timer.start();
		} else
			this.timer = null;
	}
	
	/*
	 *  function to stop the timerlistener
	 */
	public void stop() {
		if (this.timer != null)
			this.timer.stop();
	}
	/*
	 * function to start the timerlistener.
	 */
	public void start() {
		if (this.timer != null)
			this.timer.start();
	}
	
	/*
	 * Calculates displacement and heading as title suggests
	 */
	private void getDisplacementAndHeading(double[] data) {
		int leftTacho, rightTacho;
		leftTacho = leftMotor.getTachoCount();
		rightTacho = rightMotor.getTachoCount();

		data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI / 360.0;
		data[1] = (rightTacho * rightRadius - leftTacho * leftRadius) / width;
	}
	/*
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
			x += dDH[0] * Math.cos(Math.toRadians(theta));
			y += dDH[0] * Math.sin(Math.toRadians(theta));
		}

		oldDH[0] += dDH[0];
		oldDH[1] += dDH[1];
	}

	/**
	 *  return X value
	 */
	public double getX() {
		synchronized (this) {
			return x;
		}
	}

	/**
	 *  return Y value
	 */
	public double getY() {
		synchronized (this) {
			return y;
		}
	}

	/**
	 * return theta value
	 */
	public double getAng() {
		synchronized (this) {
			return theta;
		}
	}

	/**
	 *  set x,y,theta
	 * @param position an array with three entries representing the x,y and theta of the robot.
	 * @param update an array representing which values of x,y or theta shold be updated from position.
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

	/*
	 *  return x,y,theta and store in array.
	 */
	public void getPosition(double[] position) {
		synchronized (this) {
			position[0] = x;
			position[1] = y;
			position[2] = theta;
		}
	}

	/**
	 * 
	 * @return returns a new array with x,y, theta values
	 */
	public double[] getPosition() {
		synchronized (this) {
			return new double[] { x, y, theta };
		}
	}
	
	/**
	 *  accessors to motors
	 */
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}
	/**
	 * 
	 * @return the left motor is returned.
	 */
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	/**
	 * 
	 * @return the right motor is returned.
	 */
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}

	/**
	 * 
	 * @param angle angle that needs to be corrected
	 * @return an angle in range from 0 to 360 degrees
	 */
	public static double fixDegAngle(double angle) {
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);

		return angle % 360.0;
	}
	/**
	 * calculates the minimum angle from a starting position a to a final position b.
	 * @param a starting theta.
	 * @param b final theta.
	 * @return the minimum amount of turning from a to b.
	 */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);

		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
}
