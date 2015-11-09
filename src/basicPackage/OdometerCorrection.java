package basicPackage;


import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import modulePackage.LineDetection;

public class OdometerCorrection extends Thread {
	/**
	 * Clock constant
	 */
	private static final long CORRECTION_PERIOD = 10;
	/**
	 * Odometer Instance
	 */
	private Odometer odometer;
	
	/**
	 * Line detection using a derivative in order to determine if a line has been detected on the ground.
	 */
	private LineDetection lineDetector;


	/**
	 * Low value threshold for detecting a line
	 */
	private int lowValue;
	/**
	 * High value threshold for detecting a line
	 */
	private int highValue;
	/**
	 * lastValue detected by the color Sensor
	 */
	private int lastValue; 
	/**
	 * The change in the color sensor from the last value
	 */
	private int lastDerivative;

	/**
	 * Constructor for the Odometer correction
	 * @param odometer Instance of the odometer that Odometer Correction will correct
	 */
	public OdometerCorrection(Odometer odometer,LineDetection lineDetector) {
		this.odometer = odometer;
		this.lineDetector = lineDetector;
	}
	/**
	 * run odometer correction thread.
	 */
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();
			
			if(this.lineDetector.detectLine()){
				this.correctOdometer();
			}
			
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	/**
	 * calls get position and correcs the odometer heading.
	 */
	public void correctOdometer () {
		double[] position = new double[3];
		boolean[] update = {true, true, true};
		position = odometer.getPosition();
	}
}