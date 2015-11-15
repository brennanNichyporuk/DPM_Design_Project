/* 
 * Navigation.java
 * Group #: 61
 * Names: Fred Glozman (260635610) & Abdel Kader Gaye (260637736) 
 * 
 * This class contains methods which make the robot drive and turn to a specified location or direction
 */
package basicPackage;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	
	/**
	 * Constant to determine the fast speed of the robot. Set to 200
	 */
	public final static int FAST = 150;
	
	/**
	 * Constant to determine the slow speed of the robot. Set to 90 by default
	 */
	public final static int SLOW = 90;
	
	/**
	 * Constant to determine acceleration. Set to 4000
	 */
	public final static int ACCELERATION = 4000;
	
	/**
	 * Set degree error in navigation.
	 */
	private final static double DEG_ERR = 4.8;
	
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
	
	/**
	 * Function to set the motor speeds jointly
	 * @param lSpd left motor speed for float
	 * @param rSpd right motor speed for float
	 */
	public void setSpeeds(int lSpd, int rSpd) {
		this.setWheels(lSpd, rSpd);
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

	/**
	 * Function to set the motor speeds jointly
	 * @param lSpd left motor speed for int
	 * @param rSpd right motor speed for int
	 */
	private void setFloat() {
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
			this.setWheels(FAST, FAST);
		}
		this.setWheels(0, 0);
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
			this.setWheels(-FAST, -FAST);
		}
		this.setWheels(0, 0);
	}
	

	/**
	 * TurnTo function which takes an angle and boolean as arguments The boolean input controls whether or not to stop themotors when the turn is complete.
	 * @param angle The angle you want to turn to
	 * @param stop Stop the motors after having completed the turn or not. If true the motors will stop.
	 */
	public void turnTo(double angle, boolean stop) {
		if(angle<0){
			angle=Odometer.fixDegAngle(angle);
		}
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
			this.setWheels(0, 0);
		}
	}
	/**
	 * Go foward a set distance in cm.
	 * @param distance The distance you want to go forward in cm. If distance > 0 then move forward,
	 * else move backward
	 */
	public void moveStraight(double distance) {
		
		if(distance>=0){
			this.setWheels(FAST, FAST);
			leftMotor.rotate(convertDistance(Odometer.leftRadius, distance), true);
			rightMotor.rotate(convertDistance(Odometer.rightRadius, distance), false);
		}
		else{
			this.setWheels(-FAST, -FAST);
			leftMotor.rotate(convertDistance(Odometer.leftRadius, distance), true);
			rightMotor.rotate(convertDistance(Odometer.rightRadius, distance), false);
		}
		
		this.setWheels(0, 0);
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
		this.setWheels(-SLOW,SLOW);
	}
	/**
	 * causes the robot to turn right continously.
	 */
	public void turnRight(){
		this.setWheels(SLOW,-SLOW);
	}
	/**
	 * causes the robot to move forward
	 */
	public void moveForward(){
		this.setWheels(SLOW, SLOW);
	}
	/**
	 * causes the robot to move backwards
	 */
	public void moveBackward(){
		this.setWheels(-SLOW, -SLOW);
	}
	/**
	 * stops the motors
	 */
	public void stop(){
		this.setWheels(0,0);
	}
}