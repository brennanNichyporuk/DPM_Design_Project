package captureFlagPackage;

import modulePackage.*;
import basicPackage.*;

/**
 *A class which uses the robot's sensors to identify an object.
 *This class distinguishes between the target block and all the other robjects.
 *@author Fred Glozman & Abdel Kader Gaye
 */
public class IdentifyObject extends Thread
{
	
	private int objectID;
	
	private UltrasonicModule us; 
	private ColorDetection cd; 

	/**
	 *Constructor. Requires user to specify colorID value of the target flag
	 *@param objectID color value of flag 
	 *@param usm access to the ultrasonic sensor 
	 *@param lsm access to the light sensor 
	 */
	public IdentifyObject(int objectID, UltrasonicModule usm, ColorDetection cd)
	{
		this.objectID = objectID;
		this.us = usm; 
		this.cd = cd;
	}
	

	/**
	 *Overrides the run method in the Thread superclass
	 */
	@Override
	public void run()
	{

	}
	
	/**
	 *This method utilizes the robot's light sensor in order to
	 *determine the colorID value of the object directly in front of the light sensor
	 *@return returns the colorID value of the object being analyzed
	 */
	public int getObjectID()
	{
		return -1;
	}
}
