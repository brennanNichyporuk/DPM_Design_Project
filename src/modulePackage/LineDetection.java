package modulePackage;

import java.util.LinkedList;

import lejos.robotics.SampleProvider;

public class LineDetection 
{
	private SampleProvider colorSensor;
	private float[] colorData;
	
	private int last_value;
	private int last_derivative;
	private int low_value;
	private int high_value;
	private int deriv_threshhold;
	
	private LinkedList<Integer> prev = new LinkedList<Integer>();
	
	/**
	 * 
	 * @param colorSensor: Light Sensor to read values from
	 * @param colorData: Array of values
	 */
	public LineDetection(SampleProvider colorSensor, float[] colorData) 
	{
		
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		
		this.last_value = 0;
		this.last_derivative = 0;
		this.low_value = 0;
		this.high_value = 0;
		
	}
	
	/**
	 * 
	 * @return : Boolean that returns true of a line is detecting and false otherwise
	 */
	public boolean detectLine()
	{
		// Retrieve Value from Sensor And calculate new derivative
		
		colorSensor.fetchSample(colorData, 0);
		int new_value = (int)(colorData[0]*100);
		int derivative = new_value - this.last_value;
		
		// If we have an increasing derivative we are going from light to Dark
		// We want to find the highest value during this increase and the lowest as well
		
		if (derivative >= this.last_derivative) 
		{
			
			if (derivative < this.low_value) 
			{
				this.low_value = this.last_derivative;
			}
			
			if (derivative > this.high_value) 
			{
				this.high_value = derivative;
			}
		}
		
		// If we have a decreasing derivative we are going form dark to light
		// We want to compare the values of low and high to check of we have detected a line
		
		else	
		{
			if(this.high_value - this.low_value > this.deriv_threshhold)
			{
				this.high_value = 0;
				this.low_value = 0;
				return true;
			}
			
			this.high_value = 0;
			this.low_value = 0;
		}
		
		this.last_derivative = derivative;
		this.last_value = new_value;
		
		return false;
	}

}
