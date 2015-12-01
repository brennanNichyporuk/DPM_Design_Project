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
	 * error allowed in distance from a line
	 */
	private static int DISTERRMARGIN = 3;
	/**
	 * error allowed in angle
	 */
	private static int ANGLEERRMARGIN = 5;
	
	/**
	 * offset that accounts for distance and time delay between light sensor and the center of rotation 
	 */
	private static double OFFSET = 13.3;
	/**
	 * tracks if odometrycorrection should be correcting.
	 */
	public  boolean CORRECT = true;
	
	/**
	 * frequency of odometry correction
	 */
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
	 * runs the odometer correction thread. 
	 */
	public void run() {
		long correctionStart, correctionEnd;
		while (true) {
			correctionStart = System.currentTimeMillis();
			
			if(this.CORRECT){
			if(this.lineDetector.detectLine()){
					Sound.beep();
					this.correctOdometer();
				}
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
	 * corrects the odometer position based on the current position of the odometer and the direction of travel. 
	 * Only corrects if in the range of 0, 90, 180, 270, 360 degrees +- 5 degrees 
	 */
	public void correctOdometer () {
		double[] position = new double[3];
		boolean[] update = {false, false, false};
		
		position = this.odometer.getPosition();
		double x = position[0];
		double y = position[1];
		double angle = position[2];
		
		if(inRange(0,angle,ANGLEERRMARGIN)||inRange(360,angle,ANGLEERRMARGIN)){
			double nodeX = Math.round((x-OFFSET) / SQUAREDISTANCE);
			position[0]= nodeX *SQUAREDISTANCE + OFFSET;
			update[0]=true;
		}
		
		else if(inRange(90,angle,ANGLEERRMARGIN)){
			double nodeY = Math.round((y-OFFSET) / SQUAREDISTANCE);
			position[1]= nodeY * SQUAREDISTANCE + OFFSET;
			update[1] = true;
		}
		else if(inRange(180,angle,ANGLEERRMARGIN)){
			double nodeX = Math.round((x+OFFSET) / SQUAREDISTANCE);
			position[0]= nodeX *SQUAREDISTANCE - OFFSET;
			update[0]=true;
		}
		else if(inRange(270,angle,ANGLEERRMARGIN)){
			double nodeY = Math.round((y+OFFSET) / SQUAREDISTANCE);
			position[1]= nodeY *SQUAREDISTANCE - OFFSET;
			update[1]=true;
		}
		
		this.odometer.setPosition(position, update);
		}
	/**
	 * 
	 * @param target the goal value.
	 * @param value the value you want to be compared.
	 * @param relaxationFactor how much the in range function should relax the two values.
	 * @return a boolean to tell if a variable is currently in range or not.
	 */
	private boolean inRange(double target, double value, double relaxationFactor){
		if(Math.abs(target-value)<relaxationFactor){
			return true;
		}
		return false;
	}
}
