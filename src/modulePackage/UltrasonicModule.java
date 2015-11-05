package modulePackage;


import java.util.Collections;
import java.util.LinkedList;
import lejos.robotics.SampleProvider;



public class UltrasonicModule extends Thread
{
	private LinkedList<Integer> window = new LinkedList<Integer>();
	private int windowSize;
	
	private int distance;
	
	private SampleProvider us;
	private float[] usData;
	private boolean active;

	
	// Constructor
	
	/**
	 * 
	 * @param us : Ultrasonic Sensor to read values
	 */
	public UltrasonicModule(SampleProvider us)
	{
		this.us = us;
		
		this.windowSize = 5; // Size of window used in Median Filter
		
		this.active = true;
	
		for(int i =0 ; i< this.windowSize; i++)
		{
			this.window.add(0);
		}
	}
	
	/**
	 * Starts Polling thread
	 */
	public void run() 
	{
		int distance;
		
		while (true) 
		{
			if (active) 
			{
				distance = this.fetchDistance();
				this.setDistance(this.filterDistance(distance));
			}
			try { Thread.sleep(50); } catch(Exception e){}
		}
	}
	
	/**
	 * 
	 * @return un-filtered Distance
	 */
	private int fetchDistance() 
	{
		us.fetchSample(usData,0);
		int distance=(int)(usData[0]*100.0);
		return distance;
	}

	
	/**
	 * @param usData: Next value read by US Sensor
	 * @return Filtered Distance
	 **/
	
	private int filterDistance(int UsData)
	{	
		this.addValue(UsData);
		int median = this.getMedian();
		
		LinkedList<Integer> temp = this.window;
		
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
		this.window.addLast(UsData);
		this.window.removeFirst();
	}
	
	/**
	 * 
	 * @return: median of values in List
	 */
	
	private int getMedian()
	{
		LinkedList<Integer> temp = this.window;
		
		Collections.sort(temp);
		
		if(this.windowSize%2==1) return temp.get(this.windowSize/2).intValue();
		
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
		return distance;
	}
	
	/**
	 * 
	 * @param distance: New Value for Distance
	 */
	public void setDistance(int distance) 
	{
		this.distance = distance;
	}

}