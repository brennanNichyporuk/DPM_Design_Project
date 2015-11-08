package captureFlagPackage;

import java.util.ArrayList;
import java.util.Stack;

import modulePackage.*;
import basicPackage.*;

/**
 *A class which searches the environment in order to locate the target flag
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class LocateObject extends Thread
{
	//reference to the observer of this class
	private CaptureFlag captureFlag;
	
	private ArrayList<double[]> objectsLoco; 
	private Stack<double[]> blankSpaceLoco;
	private Stack<double[]> previousLocation;
			
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
	public LocateObject(CaptureFlag captureFlag, Navigation navigator, Odometer odometer, UltrasonicModule usm, ColorDetection cd)
	{
		this.captureFlag = captureFlag;
		
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
				//scan area. find objects and blank spaces
				scanArea();
				
				//if there are object to be identified. notify CaptureFlag and continue
				if(!objectsLoco.isEmpty())
				{
					captureFlag.update(ClassID.LOCATEOBJECT);
					
					try {Thread.sleep(500);} catch (InterruptedException e){}
				}
				//if there are not objects to identify and if there are blank spaces to visit. visit first blank space
				else if(objectsLoco.isEmpty() && !blankSpaceLoco.isEmpty())
				{
					//get first blank space
					double[] blankSpace = blankSpaceLoco.pop();
					
					//record current location
					previousLocation.push(odo.getPosition());
					
					//navigate to blank space
					nav.travelTo(blankSpace[0], blankSpace[1]);
					
					continue;
				}
				//no objects to identify and not blank spaces to visit. backup to previous spot
				else if (objectsLoco.isEmpty() && !blankSpaceLoco.isEmpty()){
					//get the location of the previous spot and navigate to it
					double[] previousSpot = previousLocation.pop();
					nav.travelTo(previousSpot[0], previousSpot[1]);
				}
			}
			try {Thread.sleep(500);} catch (InterruptedException e){}
		}
	}
	
	
	
	private void scanArea()
	{
		//sweep 180? 
		
		
	}

	/**
	 *Gets the location of the current object being analyzed.
	 *@return location of the current object found.
	 */
	ArrayList<double[]> getCurrentObjLoco()
	{
		return objectsLoco;
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
