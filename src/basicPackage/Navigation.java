/* 
 * Navigation.java
 * Group #: 61
 * Names: Fred Glozman (260635610) & Abdel Kader Gaye (260637736) 
 * 
 * This class contains methods which make the robot drive and turn to a specified location or direction
 */
package basicPackage;

/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	
	/**
	 * Constant to determine the fast speed of the robot. Set to 200
	 */
	public final static int FAST = 150;
	
	/**
	 * Constant to determine the slow speed of the robot. Set to 80
	 */
	public final static int SLOW = 90;
	
	/**
	 * Constant to determine acceleration. Set to 4000
	 */
	public final static int ACCELERATION = 2000;
	
	/**
	 * Set degree error in navigation.
	 */
	private final static double DEG_ERR = 4.5;
	
	/**
	 * Set distance error in navigation
	 */
	private final static double CM_ERR = 1.0;
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	

	/**
	 * Constructor for Navigation
	 * @param odo the Odometer instance which must not be null and allows for the robot
	 * to navigate.
	 */
	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.forward();
		else
			this.leftMotor.backward();
		if (rSpd < 0)
			this.rightMotor.forward();
		else
			this.rightMotor.backward();
	}
	/**
	 * Function to set the motor speeds jointly
	 * @param lSpd left motor speed for float
	 * @param rSpd right motor speed for float
	 */
	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.forward();
		else
			this.leftMotor.backward();
		if (rSpd < 0)
			this.rightMotor.forward();
		else
			this.rightMotor.backward();
	}

	/**
	 * Function to set the motor speeds jointly
	 * @param lSpd left motor speed for int
	 * @param rSpd right motor speed for int
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/**
	 * TravelTo function will travel to designated position, while constantly updating it's heading
	 * @param x The x position to travel to.
	 * @param y The y position to travel to.
	 */
	public void travelTo(double x, double y) {
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if(Math.abs(minAng-this.odometer.getAng()) > DEG_ERR){
				this.turnTo(Odometer.fixDegAngle(minAng), false);
			}
			this.setSpeeds(-FAST, -FAST);
		}
		this.setSpeeds(0, 0);
	}
	
	/**
	 * traveltoWait function will travel to designated position, while constantly updating it's heading. It will return true when it has reached the destination.
	 * @param x The x position to travel to.
	 * @param y The y position to travel to.
	 */
	public boolean travelToWait(double x, double y) {
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if(Math.abs(minAng-this.odometer.getAng()) > DEG_ERR){
				this.turnTo(Odometer.fixDegAngle(minAng), false);
			}
			this.setSpeeds(-FAST, -FAST);
		}
		this.setSpeeds(0, 0);
		return true;
	}
	
	
	
	
	
	
	/**
	 * TravelTo function will travel to designated position, while constantly updating it's heading but will travel backwards
	 * @param x The x position to travel to.
	 * @param y The y position to travel to.
	 */
	public void travelToBackwards(double x, double y){
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI)+180;
			this.turnTo(Odometer.fixDegAngle(minAng), false);
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}
	

	/**
	 * TurnTo function which takes an angle and boolean as arguments The boolean input controls whether or not to stop themotors when the turn is complete.
	 * @param angle The angle you want to turn to
	 * @param stop Stop the motors after having completed the turn or not. If true the motors will stop.
	 */
	public void turnTo(double angle, boolean stop) {
		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	/**
	 * Turns the robot angle and will only return when the robot is done turning.
	 * @param angle the angle that the robot should turn.
	 * @param stop if the robot should set the speed of the robot to zero
	 * @return returns when the robot is done turning
	 */
	public boolean turnToWait(double angle, boolean stop){
		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
		return true;
	}
	
	/**
	 * Go foward a set distance in cm.
	 * @param distance The distance you want to go forward in cm. If distance > 0 then move forward,
	 * else move backward
	 */
	public void moveStraight(double distance) {
		if(distance>=0){
			this.setSpeeds(FAST, FAST);
		}
		else{
			this.setSpeeds(-FAST, -FAST);
		}
		leftMotor.rotate(convertDistance(Odometer.width, distance), true);
		rightMotor.rotate(convertDistance(Odometer.width, distance), false);
		this.setSpeeds(0, 0);
	}
	
	
	
	/**
	 * Helper methods to convert distance into a distance that works with the wheels.
	 * convertDistance to travel to value that corresponds to wheel rotations.
	 * @param radius  radius of the wheels
	 * @param distance  distance to the destination
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	
	/**
	 * causes the robot to turn left continously.
	 */
	public void turnLeft(){
		this.setSpeeds(-SLOW,SLOW);
	}
	/**
	 * causes the robot to turn right continously.
	 */
	public void turnRight(){
		this.setSpeeds(SLOW,-SLOW);
	}
	/**
	 * causes the robot to move forward
	 */
	public void moveForward(){
		this.setSpeeds(SLOW, SLOW);
	}
	/**
	 * causes the robot to move backwards
	 */
	public void moveBackward(){
		this.setSpeeds(-SLOW, -SLOW);
	}
	/**
	 * stops the motors
	 */
	public void stop(){
		this.setSpeeds(0,0);
	}
}