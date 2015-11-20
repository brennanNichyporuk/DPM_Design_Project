package basicPackage;
import java.util.Arrays;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import modulePackage.UltrasonicModule;

public class USLocalizer {
	/**
	 * Stores possible localization types (Falling Edge and Rising Edge)
	 */
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	/**
	 * How fast the robot is allowed to rotate while localizing
	 */
	public static double ROTATION_SPEED = 30;
	private Odometer odo;
	private Navigation nav;
	private UltrasonicModule usModule;
	
	/**
	 * instantiated localization type of the USLocalizer.
	 */
	private LocalizationType locType;
	
	/**
	 * Standard threshold from the wall.
	 */
	private double distanceStandard = 40;
	/**
	 * Error margin to account for erroneous readings at the edges.
	 */
	private double errorMargin = 5;
	
	/**
	 * @param odo Odometer instance must not be null.
	 * @param nav Navigation instance must not be null.
	 * @param usSensor SampleProvider instance must not be null
	 * @param usData Storage location for usSensor, must not be null.
	 */
	public USLocalizer(Odometer odo, Navigation nav, UltrasonicModule usModule ,LocalizationType locType) {
		this.odo = odo;
		this.nav = nav;
		this.usModule = usModule;
		this.locType = locType;
	}
	/**
	 * Ultrasonic Localization routine based off of localization type set in the particular object.
	 */
	public void doLocalization() {
		double angleA, angleB, angle;
		angleA = 0;
		angleB = 0;
		angle = 0;

		if (locType == LocalizationType.FALLING_EDGE) {

			// rotate the robot until it sees no wall
			double distance = usModule.getDistance();
			while (distance < distanceStandard + errorMargin) {
				this.turn(-5.0, false);
				distance = usModule.getDistance();
			}

			// keep rotating until the robot sees a wall, then latch the angle
			while (distance >= distanceStandard - errorMargin) {
				angle = odo.getAng();
				if (distance >= distanceStandard + errorMargin)
					angleA = angle;
				this.turn(-5.0, false);
				distance = usModule.getDistance();
			}
			this.turn(0, true);
			angleA = this.averageAngle(angleA);

			// switch direction and wait until it sees no wall
			distance = usModule.getDistance();
			while (distance < distanceStandard + errorMargin) {
				this.turn(5.0, false);
				distance = usModule.getDistance();
			}

			// keep rotating until the robot sees a wall, then latch the angle
			//distance = this.getFilteredData();
			while (distance >= distanceStandard - errorMargin) {
				angle = odo.getAng();
				if (distance >= distanceStandard + errorMargin)
					angleB = angle;
				this.turn(5.0, false);
				distance = usModule.getDistance();
			}
			this.turn(0, true);
			angleB = this.averageAngle(angleB);

			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			double correctionAngle;

			if (angleA < angleB)
				correctionAngle = 45 - ((angleA + angleB) / 2);
			else
				correctionAngle = 225 - ((angleA + angleB) / 2);
			

			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, this.correctAngle(odo.getAng() + correctionAngle)}, new boolean [] {true, true, true});
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */

			// rotate the robot until it sees a wall
			double distance = usModule.getDistance();
			
			while (distance > distanceStandard - errorMargin) {
				this.turn(-5.0, false);
				distance = usModule.getDistance();
			}
			// keep rotating until the robot does not see a wall, then latch the angle
			while (distance <= distanceStandard + errorMargin) {
				angle = odo.getAng();
				if (distance <= distanceStandard - errorMargin)
					angleA = angle;
				this.turn(-5.0, false);
				distance = usModule.getDistance();
			}
			this.turn(0, true);
			angleA = this.averageAngle(angleA);

			// switch direction and wait until it sees a wall
			//distance = this.getFilteredData();
			while (distance > distanceStandard - errorMargin) {
				this.turn(5.0, false);
				distance = usModule.getDistance();
			}

			// keep rotating until the robot does not see a wall, then latch the angle
			//distance = this.getFilteredData();
			while (distance <= distanceStandard + errorMargin) {
				angle = odo.getAng();
				if (distance <= distanceStandard - errorMargin)
					angleB = angle;
				this.turn(5.0, false);
				distance = usModule.getDistance();
			}
			this.turn(0, true);
			angleB = this.averageAngle(angleB);

			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'

			double correctionAngle;

			if (angleA > angleB)
				correctionAngle = 45 - ((angleA + angleB) / 2);
			else
				correctionAngle = 225 - ((angleA + angleB) / 2);

			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, this.correctAngle(odo.getAng() + correctionAngle)}, new boolean [] {true, true, true});

		}
	}



	/**
	 * Corrects angle slightly to account for false readings at wall edges. 
	 */
	void turn(double degrees, boolean stop) {
		double angle = odo.getAng();
		angle = this.correctAngle(angle + degrees);
		nav.turnTo(angle, stop);
	}
	
	/**
	 * Correct Angle based off of localization results.
	 */
	double correctAngle(double angle){
		if (angle < 0)
			angle += 360;
		if (angle > 360)
			angle -= 360;
		return angle;
	}
	/**
	 * Average angle between input angle and the odometer instance angle.
	 */
	double averageAngle(double angle1){
		double angle2 = odo.getAng();
		// if the difference between the angles is large... 
		// the angle wrapped around (0 / 360)...
		// we must account for this
		if (Math.abs(angle2 - angle1) > 90) {
			return ((angle2 + angle1) % 360) / 2;
		}
		else
			return (angle2 + angle1) / 2;
	}
	
	public LocalizationType getLocType() {
		return locType;
	}
	public void setLocType(LocalizationType locType) {
		this.locType = locType;
	}
}
