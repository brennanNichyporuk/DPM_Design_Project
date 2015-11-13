package modulePackage;

import lejos.hardware.Button;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class ColorDetection {
	/**
	 * constant that controls how many samples the SampleProvider takes before returning the average value. 
	 */
	private static int SAMPLESIZE = 5;
	
	/**
	 * threshold constant to differentiate between a blue block and a wooden block at 2CM.
	 */
	private float THRESHOLD = 20;
	
	/**
	 * Interface with the sensor.
	 */
	private SampleProvider sampleGetColor;
	/**
	 *  Used to store samples from SampleProvider.
	 */
	float[] colorData;
	
	public ColorDetection(SensorModes sensor){
		this.sampleGetColor = sensor.getMode("RGB");
		this.colorData = new float[sampleGetColor.sampleSize()];
	}
	/**@TODO 
	 * @return returns the rgb profile of the block depending on what color it is.
	 */
	private float getData(){
		//getting the data from the sensor and averaging the value (SAMPLESIZE set at top)
		sampleGetColor.fetchSample(colorData, 0);
		
		return 0;
	}
}
