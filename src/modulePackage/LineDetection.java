package modulePackage;

import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class LineDetection 
{
	private SampleProvider colorSensor;
	private float[] colorData;
	
	private int lastValue;
	private int lastDerivative;
	private int lowValue;
	private int highValue;
	private int minDerivativeChange = 15;
	
	/**
	 * 
	 * @param colorSensor: Light Sensor to read values
	 * @param colorData: array of values
	 */
	public LineDetection(SensorModes lineSensor) 
	{
		this.colorSensor = lineSensor.getMode("Red");
		this.colorData = new float[colorSensor.sampleSize()];	
		colorSensor.fetchSample(colorData,0);
		this.lastValue = (int)(colorData[0]*100.0);	
		this.lastDerivative = 0;
		this.lowValue = 0;
		this.highValue = 0;
		
	}
	
	/**
	 * 
	 * returns when a line is detected
	 */
	public boolean detectLine() {
		colorSensor.fetchSample(colorData,0);
		int currentValue = (int)(colorData[0]*100.0);
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
		} 
		else {
			// if the magnitude of the change in the derivative is greater than 4... we have detected a line
			if (highValue - lowValue > minDerivativeChange) {
				// if we have detected a line ... we run update() which performs 
				lowValue = 0;
				highValue = 0;
				return true;
			}
			/*
			 * if the magnitude of the change in the derivative was great enough, then update()
			 * was run and highValue and lowValue was reset... otherwise it was noise and lowValue 
			 * and highValue should be reset anyway...
			 */
			lowValue = 0;
			highValue = 0;
		}
		lastDerivative = currentDerivative;
		lastValue = currentValue;
		return false;
	}
}
