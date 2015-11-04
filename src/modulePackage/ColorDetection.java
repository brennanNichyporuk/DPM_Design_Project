package modulePackage;

import lejos.hardware.Button;
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
	
	public ColorDetection(SampleProvider sampleProvideColor,float[] sampleDataColor){
		this.colorData = sampleDataColor;
		this.sampleGetColor = sampleProvideColor;
	}
	/*
	 * returns true or false if the object is a block or not a block based on the threshold.
	 */
	public boolean determineBlock(){
		float intensity = 0;
		intensity = this.getData(sampleGetColor, colorData);
		if(intensity > THRESHOLD){
			return false;
		}
		else if(intensity <= THRESHOLD && intensity > 0){
			return true;
		}
		return false;
	}
	/**
	 * @param provider
	 * @param destination
	 * @return returns the average value depending on the SAMPLESIZE constant in ColorDetection.
	 */
	private float getData(SampleProvider provider, float[] destination){
		//getting the data from the sensor and averaging the value (SAMPLESIZE set at top)
		float sum=0;
		for(int i =0;i<SAMPLESIZE;i++){
			provider.fetchSample(destination, 0);
			float intensity = destination[0] * 100;
			sum+=intensity;
		}
		return sum/SAMPLESIZE;
	}
}
