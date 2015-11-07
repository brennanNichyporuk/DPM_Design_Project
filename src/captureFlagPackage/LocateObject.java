package captureFlagPackage;

import java.util.ArrayList;

import modulePackage.*;
import basicPackage.*;

/**
 *A class which searches the environment in order to locate the target flag
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class LocateObject extends Thread
{
	//[ [x,y] ]
	private ArrayList<double[]> nonFlagLoco; 
	//[x.y]
	private double[] currentObjLoco; 
	
	private Navigation nav;	
	private Odometer odo;
	private UltrasonicModule us;
	private ColorDetection cd;
	
	//activity state variables
	private boolean isActive;
	private boolean isPaused;

	/**
	 *Constructor
	 *@param navigator contains methods which navigates the robot 
	 *@param odometer keeps track of the robot's position 
	 *@param usm access to the ultrasonic sensor 
	 *@param cd access to the light sensor. color detection feature. 
	 */
	public LocateObject(Navigation navigator, Odometer odometer, UltrasonicModule usm, ColorDetection cd)
	{
		this.nav = navigator; 
		this.odo = odometer;
		this.us = usm; 
		this.cd = cd;
		
		//initialized to active and not paused (i.e. thread will run)
		this.isActive = true;
		this.isPaused = false;
	}
	
	/**
	 *Overrides the run method in the Thread superclass
	 */
	@Override
	public void run()
	{
		while(isActive)
		{
			while(!isPaused)
			{
				
			}
			try {Thread.sleep(500);} catch (InterruptedException e){}
		}
	}

	/**
	 *Gets the location of the current object being analyzed.
	 *@return location of the current object found.
	 */
	double[] getCurrentObjLoco()
	{
		return currentObjLoco;
	}
	
	/**
	 *pauses the execution of this thread
	 */
	void pauseThread()
	{
		this.isPaused = true;
	}
	
	/**
	 *resumes the execution of this thread
	 */
	void resumeThread()
	{
		this.isPaused = false;
	}
	
	/**
	 *stops the execution of this thread
	 */
	void deactivateThread()
	{
		this.isActive = false;
	}
}
