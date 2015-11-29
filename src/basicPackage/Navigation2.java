package basicPackage;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Navigation2 extends Thread {

	private static Odometer odometer;
	private static EV3LargeRegulatedMotor leftMotor;
	private static EV3LargeRegulatedMotor rightMotor;

	private final double LEFT_WHEEL_RADIUS, RIGHT_WHEEL_RADIUS, TRACK;
	private final int RIGHT_ROTATE_SPEED, LEFT_ROTATE_SPEED, RIGHT_FORWARD_SPEED, LEFT_FORWARD_SPEED;

	Navigation2 (Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, 
			int ROTATE_SPEED, int FORWARD_SPEED, double LEFT_WHEEL_RADIUS, double RIGHT_WHEEL_RADIUS,
			double TRACK) {
		Navigation2.odometer = odometer;
		Navigation2.leftMotor = leftMotor;
		Navigation2.rightMotor = rightMotor;
		this.LEFT_WHEEL_RADIUS = LEFT_WHEEL_RADIUS;
		this.RIGHT_WHEEL_RADIUS = RIGHT_WHEEL_RADIUS;
		this.TRACK = TRACK;
		
		// Adjust forward speed and rotate speed depending of wheel radius of each wheel
		this.LEFT_ROTATE_SPEED = (int) ((ROTATE_SPEED * 360) / (2 * Math.PI * LEFT_WHEEL_RADIUS));
		this.RIGHT_ROTATE_SPEED = (int) ((ROTATE_SPEED * 360) / (2 * Math.PI * RIGHT_WHEEL_RADIUS));
		this.LEFT_FORWARD_SPEED = (int) ((FORWARD_SPEED * 360) / (2 * Math.PI * LEFT_WHEEL_RADIUS));
		this.RIGHT_FORWARD_SPEED = (int) ((FORWARD_SPEED * 360) / (2 * Math.PI * RIGHT_WHEEL_RADIUS));
	}


	void travelTo (double desiredX, double desiredY) {

		double[] position = odometer.getPosition();

		double xError = desiredX - position[0];
		double yError = desiredY - position[1];
		double desiredTheta = Math.toDegrees(Math.atan2(yError, xError)); 

		// turn to desiredTheta
		this.turnTo(position[2], desiredTheta);
		
		double linearError = Math.sqrt((xError * xError) + (yError * yError));

		leftMotor.setSpeed(LEFT_FORWARD_SPEED);
		rightMotor.setSpeed(RIGHT_FORWARD_SPEED);

		leftMotor.rotate(convertDistance(LEFT_WHEEL_RADIUS, linearError), true);
		rightMotor.rotate(convertDistance(RIGHT_WHEEL_RADIUS, linearError), false);

	}

	void turnTo (double currentTheta, double desiredTheta) {

		double errorTheta = desiredTheta - currentTheta;

		if (errorTheta < -180) {
			errorTheta += 360;
		}
		else if (errorTheta > 180) {
			errorTheta -= 360;
		}

		leftMotor.setSpeed(LEFT_ROTATE_SPEED);
		rightMotor.setSpeed(RIGHT_ROTATE_SPEED);

		leftMotor.rotate(-convertAngle(LEFT_WHEEL_RADIUS, TRACK, errorTheta), true);
		rightMotor.rotate(convertAngle(RIGHT_WHEEL_RADIUS, TRACK, errorTheta), false);

	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

}
