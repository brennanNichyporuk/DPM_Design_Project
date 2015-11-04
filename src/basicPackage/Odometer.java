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

import java.util.Arrays;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer implements TimerListener {
	
	/**
	 * Acts as the clock for the odometer. 
	 * Defaults to 20ms if if the timeout interval is given as <= 0.
	 */
	private Timer timer;
	
	/**
	 * Left and right motors. Both must not be null and can be initialized using 
	 * constructor.
	 */
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	
	/**
	 * Timeout being for the clock in ms.
	 */
	private final int DEFAULT_TIMEOUT_PERIOD = 20;
	
	/**
	 * Instance variables for car dimensions.
	 */
	private double leftRadius, rightRadius, width;
	
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
	 */
	public Odometer (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int INTERVAL, boolean autostart) {
		
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		
		// default values, modify for your robot
		this.rightRadius = 2.0;
		this.leftRadius = 2.0;
		this.width = 12.1;
		
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

			x += dDH[0] * Math.cos(Math.toRadians(theta));
			y += dDH[0] * Math.sin(Math.toRadians(theta));
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
			//System.out.println(Arrays.toString(position));
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
		
		//System.out.println(Arrays.toString(this.getPosition()));
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
	
	/**
	 * accessors for both motors
	 */
	public EV3LargeRegulatedMotor [] getMotors() {
		return new EV3LargeRegulatedMotor[] {this.leftMotor, this.rightMotor};
	}
	/**
	 * asscessors for left motor
	 */
	public EV3LargeRegulatedMotor getLeftMotor() {
		return this.leftMotor;
	}
	/**
	 * accessors for right motor
	 */
	public EV3LargeRegulatedMotor getRightMotor() {
		return this.rightMotor;
	}

	/**
	 * Helper method to fix a degree angle (ensure we never pass a negative value)
	 */
	public static double fixDegAngle(double angle) {
		if (angle < 0.0)
			angle = 360.0 + (angle % 360.0);

		return angle % 360.0;
	}
	/**
	 * Calculate the minimum angle based on the difference between the two angles
	 * */
	public static double minimumAngleFromTo(double a, double b) {
		double d = fixDegAngle(b - a);

		if (d < 180.0)
			return d;
		else
			return d - 360.0;
	}
}
