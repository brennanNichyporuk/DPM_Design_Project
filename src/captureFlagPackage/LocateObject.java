package captureFlagPackage;

import java.util.ArrayList;

import modulePackage.*;
import basicPackage.*;

/**
 *A class which searches the environment in order to locate the target flag
 *@author Fred Glozman & Abdel Kader Gaye
 */
public class LocateObject extends Thread
{
	private ArrayList nonFlagLoco; 
	private Block currentObjLoco; 
	
	private Navigation nav;	
	private Odometer odo;
	private UltrasonicModule us;
	private ColorDetection cd;

	/**
	 *Constructor
	 *@param navigator contains methods which navigates the robot 
	 *@param odometer keeps track of the robot's position 
	 *@param usm access to the ultrasonic sensor 
	 *@param lsm access to the light sensor 
	 */
	public LocateObject(Navigation navigator, Odometer odometer, UltrasonicModule usm, ColorDetection cd)
	{
		this.nav = navigator; 
		this.odo = odometer;
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
	 *@return location of the current object found
	 */
	public Block getCurrentObjLoco()
	{
		return new Block();
	}
}
