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
	/*
	 * Constant to determine the fast speed of the robot. Set to 200
	 */
	final static int FAST = 200;
	/*
	 * Constant to determine the slow speed of the robot. Set to 80
	 */
	final static int SLOW = 80;
	/*
	 * Constant to determine acceleration. Set to 4000
	 */
	final static int ACCELERATION = 4000;
	
	/*
	 * Set degree error in navigation.
	 */
	final static double DEG_ERR = 3.0;
	/*
	 * Set distance error in navigation
	 */
	final static double  CM_ERR = 1.0;
	/*
	 * Odometer to allow for navigator to make calls to odometer 
	 * to determine where to navigate to.
	 */
	private Odometer odometer;
	/*
	 * Left Motor instance
	 */
	private EV3LargeRegulatedMotor leftMotor;
	/*
	 * Right Motor instance
	 */
	private EV3LargeRegulatedMotor rightMotor;
	
	/*
	 * Constant to set the wheelbase of the robot (cm)
	 */
	public static final double WB=12.0; 
	/*
	 * Constant to set the wheel radius of the robot(cm)
	 */
	public static final double WR=2.2; 
	
	/*
	 * Error margin in distance calculations
	 */
	private static final double ERR_MARGIN = 0.5;

	/*
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
	 * Function to set the motor speeds jointly
	 * @param lSpd left motor speed for float
	 * @param rSpd right motor speed for float
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
	
	/*
	 * Function to set the motor speeds jointly
	 * @param lSpd left motor speed for int
	 * @param rSpd right motor speed for int
	 */
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
	 * TravelTo function will travel to designated position, while constantly updating it's heading
	 * @param x The x position to travel to.
	 * @param y The y position to travel to.
	 */
	public void travelTo(double x, double y) {
		double minAng;
		//System.out.println("Traveling to x: "+x +" y: "+y);
		//System.out.println("Traveling from x: "+Math.round(odometer.getX()) +" y: "+Math.round(odometer.getY()) + " theta: "+ Math.round(this.odometer.getAng()));
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0) minAng += 360.0;
		this.turnTo(minAng, false);
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			
			this.setSpeeds(SLOW, SLOW);
		}
		this.setSpeeds(0, 0);
	}
	/*
	 * Travels to designated position but backwards. MUST IMPLEMENT
	 * @param x The x position to travel to
	 * @param y The y position to travel to
	 * 
	 */
	public void travelToBackwards(double x, double y) {
		double minAng;
		minAng = (Math.atan2(y - odometer.getY(), x - odometer.getX())) * (180.0 / Math.PI);
		if (minAng < 0) minAng += 360.0;
		this.turnTo(minAng, false);
		while (Math.abs(x - odometer.getX()) > CM_ERR || Math.abs(y - odometer.getY()) > CM_ERR) {
			
			this.setSpeeds(SLOW, SLOW);
		}
		this.setSpeeds(0, 0);
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean input controls whether or not to stop themotors when the turn is complete.
	 * @param angle The angle you want to turn to
	 * @param boolean Stop the motors after having completed the turn or not.
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
	
	/*
	 *  Method to help calculate the right angle that we want to turn to given a x and y.
	 *  @param x The relative x position.
	 *  @param y The relative y position.
	 */
	public double calculateAngle(double x, double y){
		// Get Current Position
		double currentX = this.odometer.getX();
		double currentY = this.odometer.getY();
		// Make Vector
		double vector[] = new double[2];
		
		vector[0] = Math.round(x - currentX);
		vector[1] = Math.round(y - currentY);
		
		// Calculate Vector's angle relative to origin
		
		double angle=0;
		
		// 
		if(vector[0]==0 && vector[1]==0) angle = 0;
		
		else if(this.inRange(vector[1], 0, ERR_MARGIN))
		{
			if(vector[0]>0) angle = 90;
			
			else angle = -90;
		}
		
		else if(vector[1]<0 && vector[0]>0)
		{
			angle = Math.toDegrees( Math.atan(vector[0]/vector[1]) ) + 180;
		}
		
		else if(vector[0]<0 && vector[1]<0)
		{
			angle = Math.toDegrees( Math.atan(vector[1]/vector[0]) ) - 180;
		}
		
		else if(vector[0]>0)
		{
			angle = Math.toDegrees( Math.atan(vector[0]/vector[1]) );
		}
		return angle;
	}

	/*
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
		leftMotor.rotate(convertDistance(WR, distance), true);
		rightMotor.rotate(convertDistance(WR, distance), false);
		this.setSpeeds(0, 0);
	}
	
	/*
	 * Helper methods to convert distance into a distance that works with the wheels.
	 * convertDistance to travel to value that corresponds to wheel rotations.
	 * @param radius  radius of the wheels
	 * @param distance  distance to the destination
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	
	/*
	 * Helper method to convert the angle to a distance that works with the wheels.
	 * @param radius the radius of the wheels
	 * @param width the width of the car
	 * @param angle the angle that you want to turn to.
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	/*
	 * Tells if a value is in range.
	 * @param val the value you want to see if it is in range
	 * @param target the target value
	 * @param absRange the greatest absolute difference between your target value and the actual value.
	 */
	private boolean inRange(double val, double target, double absRange){
		if(Math.abs(val-target)< absRange){
			return true;
		}
		
		else{	
			return false;
		}
	}
		
}

