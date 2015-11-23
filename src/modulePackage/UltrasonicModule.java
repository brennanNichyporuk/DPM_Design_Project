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
	public LinkedList<Integer> window = new LinkedList<Integer>();
	private int windowSize;
	
	private int distance;
	
	private EV3MediumRegulatedMotor neck;
	private SampleProvider us;
	private float[] usData;
	private int sensorAngle = 0;
	
	private final int ROTATE_SPEED = 100;

	
	// Constructor
	
	/**
	 * 
	 * @param us: Ultrasonic Sensor to read distance
	 * @param neck: Medium Motor used to turn US Sensor
	 */
	public UltrasonicModule(SampleProvider us, float[] usData, EV3MediumRegulatedMotor neck)
	{
		this.us = us;
		this.usData = usData;
		this.neck = neck;
		
		this.windowSize = 5; // Size of window used in Median Filter
		for (int i = 0; i < this.windowSize; i++) {
			this.addValue(this.fetchDistance());
		}
	}
	
	
	

	/**
	 * 
	 * @return un-filtered Distance
	 */
	public int fetchDistance() 
	{
		us.fetchSample(this.usData,0);
		int dist=(int)(this.usData[0]*100.0);
		
		if(dist>255) return 255;
		return dist;
	}

	
	/**
	 * @param usData: Next value read by US Sensor
	 * @return Filtered Distance
	 **/
	
	private int filterDistance(int UsData)
	{	
		this.addValue(UsData);
		int median = this.getMedian();
		
		@SuppressWarnings("unchecked")
		LinkedList<Integer> temp = (LinkedList<Integer>) this.window.clone();
		
		for(int i = 0; i<temp.size(); i++)
		{
			if(temp.get(i)>median)
			{
				temp.set(i, median);
			}
		}
		
		
		return this.getAverage(temp);
	}
	
	/**
	 * @param UsData: Value to be added to window
	 * @return: void
	 */
	
	private void addValue(int UsData)
	{	
		if(this.window.size()<this.windowSize)
		{
			this.window.add(UsData);
		}
		else
		{
			this.window.removeFirst();
			this.window.addLast(UsData);
		}
		
	}
	
	/**
	 * 
	 * @return: median of values in List
	 */
	
	public int getMedian()
	{
		@SuppressWarnings("unchecked")
		LinkedList<Integer> temp = (LinkedList<Integer>) this.window.clone();
		
		Collections.sort(temp);
		
		if(temp.size()==1)
		{
			return temp.get(0);
		}
		
		else if(temp.size()==2)
		{
			return (temp.get(0)+temp.get(1))/2;
		}
		
		else if(this.windowSize%2==1) return temp.get(this.windowSize/2).intValue();
		
		else return (temp.get(this.windowSize/2).intValue() + temp.get( (this.windowSize/2)+1 ).intValue() ) / 2;
	}
	
	/**
	 * @param filtered_val: filtered list of values
	 * @return: average of filtered values in in window
	 */
	
	private int getAverage(LinkedList<Integer> filtered_val)
	{
		float sum = 0;
		
		
		for(Integer val: filtered_val)
		{
			sum += val;
		}
		
		int sumInt = (int)sum; // Cast to int before division to avoid Numerical cancellation error
		
		return sumInt/this.windowSize;
	}

	// Call this to retrieve Sensors Current Reading
	/**
	 * 
	 * @return Filtered Distance
	 */
	public int getDistance() 
	{
		int un_filtered = this.fetchDistance();
		this.setDistance(this.filterDistance(un_filtered));
		return this.distance;
	}
	
	/**
	 * 
	 * @param distance: New Value for Distance
	 */
	public void setDistance(int distance) 
	{
		this.distance = distance;
	}

	
	/**
	 * 
	 * @param angle: angle to turn Sensor
	 */
	public void rotateSensorToWait(double angle)
	{
		int adj = (int)angle - this.getSensorAngle();
		
		if(adj<0)
		{
			this.neck.setSpeed(ROTATE_SPEED);
			this.neck.rotate(adj, false);
		} 
		
		else
		{	
			this.neck.setSpeed(-ROTATE_SPEED);
			this.neck.rotate(adj, false);
		}
		
		this.setSensorAngle((int)angle); 
	}
	
	public void rotateSensorTo(double angle)
	{
		int adj = (int)angle - this.getSensorAngle();
		
		if(adj<0)
		{
			this.neck.setSpeed(ROTATE_SPEED);
			this.neck.rotate(adj, true);
		} 
		
		else
		{	
			this.neck.setSpeed(-ROTATE_SPEED);
			this.neck.rotate(adj, true);
		}
		
		this.setSensorAngle((int)angle); 
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


