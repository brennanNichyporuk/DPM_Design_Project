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
	private float[] colorData;
	
	public ColorDetection(SensorModes sampleProvideColor)
	{
		this.sampleGetColor = sampleProvideColor.getMode("ColorID");
		this.colorData = new float[sampleGetColor.sampleSize()];
		this.sampleGetColor.fetchSample(this.colorData,0);
	}
	/**
	 * @param provider
	 * @param destination
	 * @return returns the average value depending on the SAMPLESIZE constant in ColorDetection.
	 */
	public int getData()
	{
		// Getting the data from the sensor and averaging the value (SAMPLESIZE set at top)
		int sum=0;
		for(int i =0;i<SAMPLESIZE;i++)
		{
			this.sampleGetColor.fetchSample(this.colorData, 0);
			int intensity = (int)(this.colorData[0]);
			sum+=intensity;
		}
		
		return sum/SAMPLESIZE;
	}
}