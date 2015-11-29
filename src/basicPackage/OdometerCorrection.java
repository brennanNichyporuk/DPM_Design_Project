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
	private static final long CORRECTION_PERIOD = 50;
	/**
	 * Odometer Instance
	 */
	private Odometer odometer;
	
	/**
	 * Line detection using a derivative in order to determine if a line has been detected on the ground.
	 */
	private LineDetection lineDetector;
	
	
	/**
	 * distance between squares. Set by default to 30 centimeters
	 */
	public static double SQUAREDISTANCE = 30.48;
	
	/**
	 * error in odometry correction
	 */
	private int DISTERRMARGIN = 10;
	
	
	private long TIME_MARGIN = 1000;
	
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
	 * calls get position and corrects the odometer heading.
	 */
	public void correctOdometer () {
		double[] position = new double[3];
		boolean[] update = {false, false, false};
		
		position = this.odometer.getPosition();
		double x = position[0];
		double y = position[1];
		
		//testing to see if we are near 30 in the x
		if((x % SQUAREDISTANCE) < DISTERRMARGIN || (x % SQUAREDISTANCE) > (SQUAREDISTANCE - DISTERRMARGIN)){
			double multipleX = Math.round( x / SQUAREDISTANCE);
			position[0] = multipleX * SQUAREDISTANCE;
			update[0] = true; 
			System.out.println("Set to x: "+position[0]);
		}
		
		//testing to see if we are near 30 in the y
		if((y % SQUAREDISTANCE) < DISTERRMARGIN || (y % SQUAREDISTANCE) > (SQUAREDISTANCE - DISTERRMARGIN)){
			double multipleY = Math.round( y / SQUAREDISTANCE);
			position[1] = multipleY * SQUAREDISTANCE;
			update[1] = true;
			System.out.println("Set to y: "+position[1]);
		}
		this.odometer.setPosition(position, update);
	}
}
