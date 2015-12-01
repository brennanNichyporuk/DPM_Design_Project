package basicPackage;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Navigation extends Thread {
	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;
	private static GyroCorrection gyroCorrecter;
	private static OdometerCorrection odometryCorrecter;
	private final double LEFT_WHEEL_RADIUS, RIGHT_WHEEL_RADIUS, TRACK;
	private final int RIGHT_ROTATE_SPEED, LEFT_ROTATE_SPEED, RIGHT_FORWARD_SPEED, LEFT_FORWARD_SPEED;
	public final static int SLOW = 120; 
	private final static double DEG_ERR = 3;
	
	/**
	 * 
	 * @param odometer
	 * @param leftMotor
	 * @param rightMotor
	 * @param ROTATE_SPEED should be around 4-6 
	 * @param FORWARD_SPEED should be around 4-6
	 * @param LEFT_WHEEL_RADIUS
	 * @param RIGHT_WHEEL_RADIUS
	 * @param TRACK
	 */
	public Navigation (Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
			int ROTATE_SPEED, int FORWARD_SPEED, double LEFT_WHEEL_RADIUS, double RIGHT_WHEEL_RADIUS,
			double TRACK, GyroCorrection gyroCorrecter, OdometerCorrection odometryCorrecter) {
		Navigation.odometer = odometer;
		Navigation.leftMotor = leftMotor;
		Navigation.rightMotor = rightMotor;
		this.LEFT_WHEEL_RADIUS = LEFT_WHEEL_RADIUS;
		this.RIGHT_WHEEL_RADIUS = RIGHT_WHEEL_RADIUS;
		this.TRACK = TRACK;
		
		// Adjust forward speed and rotate speed depending of wheel radius of each wheel
		this.LEFT_ROTATE_SPEED = (int) ((ROTATE_SPEED * 360) / (2 * Math.PI * LEFT_WHEEL_RADIUS));
		this.RIGHT_ROTATE_SPEED = (int) ((ROTATE_SPEED * 360) / (2 * Math.PI * RIGHT_WHEEL_RADIUS));
		this.LEFT_FORWARD_SPEED = (int) ((FORWARD_SPEED * 360) / (2 * Math.PI * LEFT_WHEEL_RADIUS));
		this.RIGHT_FORWARD_SPEED = (int) ((FORWARD_SPEED * 360) / (2 * Math.PI * RIGHT_WHEEL_RADIUS));
		this.gyroCorrecter = gyroCorrecter;
		this.odometryCorrecter = odometryCorrecter;
	}
	
	public void moveStraight(double distance) {
		
	}
	
	public void setSpeeds(float lSpd, float rSpd) {
		leftMotor.setSpeed(lSpd);
		rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			leftMotor.backward();
		else
			leftMotor.forward();
		if (rSpd < 0)
			rightMotor.backward();
		else
			rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		leftMotor.setSpeed(lSpd);
		rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			leftMotor.backward();
		else
			leftMotor.forward();
		if (rSpd < 0)
			rightMotor.backward();
		else
			rightMotor.forward();
	}

	public void travelTo (double desiredX, double desiredY, boolean useGyro) {
		leftMotor.setAcceleration(2000);
		rightMotor.setAcceleration(2000);
		
		if(useGyro){
			double[] angleSet = {0,0,gyroCorrecter.correctOrientation()};
			boolean[] update = {false,false,true};
			odometer.setPosition(angleSet, update);
		}
		
		double[] position = odometer.getPosition();
		double xError = desiredX - position[0];
		double yError = desiredY - position[1];
		double desiredTheta = Math.toDegrees(Math.atan2(yError, xError)); 

		// turn to desiredTheta
		this.turnTo(desiredTheta,true);
		
		double linearError = Math.sqrt((xError * xError) + (yError * yError));

		leftMotor.setSpeed(LEFT_FORWARD_SPEED);
		rightMotor.setSpeed(RIGHT_FORWARD_SPEED);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		leftMotor.rotate(convertDistance(LEFT_WHEEL_RADIUS, linearError), true);
		rightMotor.rotate(convertDistance(RIGHT_WHEEL_RADIUS, linearError), false);

	}

	public void turnTo (double desiredTheta, boolean stop) {
		leftMotor.setAcceleration(2000);
		rightMotor.setAcceleration(2000);
		this.odometryCorrecter.CORRECT = false;
		
		double currentTheta = odometer.getAng();
		double errorTheta = desiredTheta - currentTheta;

		if (errorTheta < -180) {
			errorTheta += 360;
		}
		else if (errorTheta > 180) {
			errorTheta -= 360;
		}

		leftMotor.setSpeed(LEFT_ROTATE_SPEED);
		rightMotor.setSpeed(RIGHT_ROTATE_SPEED);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		leftMotor.rotate(-convertAngle(LEFT_WHEEL_RADIUS, TRACK, errorTheta), true);
		rightMotor.rotate(convertAngle(RIGHT_WHEEL_RADIUS, TRACK, errorTheta), false);
		if(stop){
			this.setSpeeds(0, 0);
		}
		
		this.odometryCorrecter.CORRECT = true;
	}
	
	public void turnToContinous(double angle, boolean stop) {
		double error = angle - odometer.getAng();
		
		while (Math.abs(error) > DEG_ERR) {
			error = angle - odometer.getAng();
			
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

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
	//turns the robot left. 
	public void turnLeft(){
		this.setSpeeds(-this.LEFT_ROTATE_SPEED,this.RIGHT_ROTATE_SPEED);
	}
	//turns the robot right
	public void turnRight(){
		this.setSpeeds(this.LEFT_ROTATE_SPEED,-this.RIGHT_ROTATE_SPEED);
	}
	//move forward
	public void moveForward(){
		this.setSpeeds(this.LEFT_FORWARD_SPEED,this.RIGHT_FORWARD_SPEED);
	}
	//move backward
	public void moveBackward(){
		this.setSpeeds(-this.LEFT_FORWARD_SPEED,-this.RIGHT_FORWARD_SPEED);
	}
	//stops the robot motors
	public void stopMoving(){
		this.setSpeeds(0,0);
	}
	
	public int cm_to_seconds(int distance)
	{
		return (int) ( (distance/(2*Math.PI*odometer.leftRadius)) * 360 )/SLOW;
	}

}