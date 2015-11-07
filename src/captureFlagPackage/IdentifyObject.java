package captureFlagPackage;

import modulePackage.*;
import basicPackage.*;

/**
 *A class which uses the robot's sensors to identify an object.
 *This class distinguishes between the target block and all the other robjects.
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class IdentifyObject extends Thread
{	
	/**
	 * a value of -1 signifies that the light sensor failed to get a reading.
	 * acceptable values are in the range [0, 13].
	 */
	private int objectID;
	
	private UltrasonicModule us; 
	private ColorDetection cd; 
	
	//activity state variables
	private boolean isActive;
	private boolean isPaused;


	/**
	 *Constructor. Requires user to specify colorID value of the target flag
	 *@param usm access to the ultrasonic sensor 
	 *@param cd access to the light sensor. color detection feature. 
	 */
	public IdentifyObject(UltrasonicModule usm, ColorDetection cd)
	{
		this.us = usm; 
		this.cd = cd;
		
		//initialize objectID
		objectID = -1;
		
		//initialized to active and paused (i.e. thread will be idle)
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
	 *This method utilizes the robot's light sensor in order to
	 *determine the colorID value of the object directly in front of the light sensor
	 *@return returns the colorID value of the object being analyzed
	 */
	int getObjectID()
	{
		return objectID;
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
