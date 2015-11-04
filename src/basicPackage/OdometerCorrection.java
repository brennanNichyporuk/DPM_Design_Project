package basicPackage;


import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class OdometerCorrection extends Thread {
	/*
	 * Clock constant
	 */
	private static final long CORRECTION_PERIOD = 10;
	/*
	 * Odometer Instance
	 */
	private Odometer odometer;
	/*
	 * Color sensor instance to get light readings
	 */
	private static final Port colorPort = LocalEV3.get().getPort("S1");


	/*
	 * Low value threshold for detecting a line
	 */
	private int lowValue;
	/*
	 * High value threshold for detecting a line
	 */
	private int highValue;
	/*
	 * lastValue detected by the color Sensor
	 */
	private int lastValue; 
	/*
	 * The change in the color sensor from the last value
	 */
	private int lastDerivative;

	/*
	 * Constructor for the Odometer correction
	 * @param odometer Instance of the odometer that Odometer Correction will correct
	 */
	public OdometerCorrection(Odometer odometer) {
		this.odometer = odometer;

		

		//colorSensorColor.fetchSample(colorData,0);
		//lastValue = (int)(colorData[0]*100.0);
		//colorSensorColor.fetchSample(colorData,0);
		//lastValue = (int)(colorData[0]*100.0);
		//colorSensorColor.fetchSample(colorData,0);
		//lastValue = (int)(colorData[0]*100.0);
		
		lowValue = 0;
		highValue = 0;
		lastDerivative = 0;

	}
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// put your correction code here
			//colorSensorColor.fetchSample(colorData,0);
			int currentValue = 0;   /* (int)(colorData[0]*100.0);*/
			int currentDerivative = currentValue - lastValue;

			// if the derivative is increasing...
			if (currentDerivative >= lastDerivative) {
				// set the lowValue to the minimum value of the derivative (lastDerivative)
				if (currentDerivative < lowValue) {
					lowValue = lastDerivative;
				}
				// similarly... set highValue to the maximum value of the derivative...
				if (currentDerivative > highValue) {
					highValue = currentDerivative;
				}
			} else {

				// if the magnitude of the change in the derivative is greater than 4...
				if (highValue - lowValue > 4) {
					this.correctOdometer();
				}
				
				/*
				 * if the magnitude of the change in the derivative was great enough, then correctOdometer()
				 * was run and highValue and lowValue was reset... otherwise it was noise and lowValue 
				 * and highValue should be reset anyway...
				 */
				lowValue = 0;
				highValue = 0;
			}

			lastDerivative = currentDerivative;
			lastValue = currentValue;
			// this ensure the odometry correction occurs only once every period
			
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
	/*
	 * calls get position and correcs the odometer heading.
	 */
	public void correctOdometer () {
		double[] position = new double[3];
		boolean[] update = {true, true, true};
		position = odometer.getPosition();
		//update correctOdometer
	}
}