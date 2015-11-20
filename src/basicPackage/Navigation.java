package basicPackage;
/* 
 * Navigation.java
 * Group #: 61
 * Names: Fred Glozman (260635610) & Abdel Kader Gaye (260637736) 
 * 
 * This class contains methods which make the robot drive and turn to a specified location or direction
 */


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
	private final static int FAST = 100, SLOW = 90, ACCELERATION = 500;
	

	private final static double DEG_ERR = 3.5, CM_ERR = 1.0;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;

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
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x, double y) {
		double minAng;
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			this.turnTo(minAng, false);
			this.setSpeeds(FAST, FAST);
		}
		this.setSpeeds(0, 0);
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		
		
		if(inRange(angle,0,3.5)||inRange(angle,360,3.5)){
			if(inRange(this.odometer.getAng(),0,3.5)){
				error=angle - 360 - this.odometer.getAng();
			}
			else if(inRange(this.odometer.getAng(),360,3.5)){
				error = angle -this.odometer.getAng()+360;
			}
		}

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
	
	public void moveStraight(double distance) {
		
		if(distance>=0){
			this.setWheels(FAST, FAST);
			leftMotor.rotate(convertDistance(odometer.leftRadius, distance), true);
			rightMotor.rotate(convertDistance(odometer.rightRadius, distance), false);
		}
		else{
			this.setWheels(-FAST, -FAST);
			leftMotor.rotate(convertDistance(odometer.leftRadius, distance), true);
			rightMotor.rotate(convertDistance(odometer.rightRadius, distance), false);
		}
		
		this.setWheels(0, 0);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	private void setWheels(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}
	
	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		this.travelTo(Math.cos(Math.toRadians(this.odometer.getAng())) * distance, Math.cos(Math.toRadians(this.odometer.getAng())) * distance);

	}
	
	//turns the robot left. 
	public void turnLeft(){
		this.setSpeeds(-SLOW,SLOW);
	}
	//turns the robot right
	public void turnRight(){
		this.setSpeeds(SLOW,-SLOW);
	}
	//move forward
	public void moveForward(){
		this.setSpeeds(SLOW, SLOW);
	}
	//move backward
	public void moveBackward(){
		this.setSpeeds(-SLOW, -SLOW);
	}
	//stops the robot motors
	public void stop(){
		this.setSpeeds(0,0);
	}
	
	
	public int cm_to_seconds(int distance)
	{
		return (int) ( (distance/(2*Math.PI*odometer.leftRadius)) * 360 )/SLOW;
	}
	
	
	
	private boolean inRange(double val, double target, double absRange)
	{

		if(Math.abs(val-target)< absRange)
		{
			return true;
		}
		
		else
		{	
			return false;
		}
		
	}
	
	
}
