package modulePackage;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.robotics.SampleProvider;


/**
 * 
 * @author thomaskaratzas
 *
 */


public class UltrasonicModule
{
	private LinkedList<Integer> window = new LinkedList<Integer>();
	private int windowSize;

	private int distance;

	private SampleProvider us;
	private float[] usData;
	private int sensorAngle = 0;

	private final int ROTATE_SPEED = 100;
	private int[] filter;
	private int filterWidth;
	private int filterPointer;


	// Constructor

	/**
	 * 
	 * @param usData 
	 * @param us: Ultrasonic Sensor to read distance
	 * @param neck: Medium Motor used to turn US Sensor
	 */
	public UltrasonicModule(SampleProvider us, float[] usData,EV3MediumRegulatedMotor neck)
	{
		this.us = us;
		this.usData = usData;

		this.windowSize = 5; // Size of window used in Median Filter
		this.filterWidth = 10;
		this.filterPointer = 0;

		this.filter = new int[filterWidth];
		for (int i = 0; i < filterWidth; i++) {
			this.filter(this.fetchDistance());
		}

		//initialize the ultrasonic sensor
		/*
		for(int i =0;i<windowSize;i++){
			this.addValue(this.fetchDistance());
		}
		 */
	}


	/**
	 * 
	 * @return un-filtered Distance
	 */
	private int fetchDistance() 
	{
		us.fetchSample(this.usData,0);
		int dist=(int)(this.usData[0]*100.0);
		return dist;
	}

	/**
	 * 
	 * @return: median of values in List
	 */

	// MEDIAN FILTER...
	public int filter(int distance) {

		this.storeDistance(distance);

		int[] filterCopy = filter.clone();
		Arrays.sort(filterCopy);

		int median;

		if (filterWidth % 2 == 1)
			median = filterCopy[filterWidth / 2];
		else
			median = ((filterCopy[filterWidth / 2] + filterCopy[(filterWidth / 2) + 1]) / 2);

		int average = 0;

		// replace anything greater than the median
		for (int i = 0; i < filterWidth; i++) {
			if (filterCopy[i] > median)
				average += median;
			else
				average += filterCopy[i];
		}

		// calculate the average
		average /= filterWidth;

		System.out.println(average);
		
		return average;
	}

	public void storeDistance(int distance) {
		if (Math.abs(distance) > 255)
			distance = 255;

		filter[filterPointer] = distance;
		filterPointer++;
		filterPointer = filterPointer % filterWidth;
	}

	

	

	// Call this to retrieve Sensors Current Reading
	/**
	 * 
	 * @return Filtered Distance
	 */

	public int getDistance() {
		return this.filter(this.fetchDistance());
	}

	/*
	public int getDistance() 
	{
		int un_filtered = this.fetchDistance();
		this.setDistance(this.filterDistance(un_filtered));
		System.out.println(this.distance);
		return this.distance;
	}
	 */

	
	/**
	 * 
	 * @param distance: New Value for Distance
	 */
	public void setDistance(int distance) 
	{
		this.distance = distance;
	}


	public int getSensorAngle() 
	{
		return sensorAngle;
	}


	public void setSensorAngle(int sensorAngle) 
	{
		this.sensorAngle = sensorAngle;
	}
}