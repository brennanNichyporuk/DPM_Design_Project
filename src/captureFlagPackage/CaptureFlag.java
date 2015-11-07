package captureFlagPackage;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import modulePackage.*;
import basicPackage.*;

/**
 *A class which is responsible for locating and capturing the flag 
 *@author Fred Glozman, Abdel Kader Gaye 
 */
public class CaptureFlag extends Thread implements IObserver
{	
	//colorID value of the target flag. want to locate and capture the object with this colorID
	private final int targetFlagColorID; 
	
	//locates objects
	private LocateObject locator;
	
	//identifies objects
	private IdentifyObject identifier;
	
	//grabs object
	private PickupObject grabber; 
		
	//robot navigation and sensing
	private Navigation nav;	
	private Odometer odo;
	private UltrasonicModule us; 
	private ColorDetection cd; 
	
	//motor which control's robot's arm
	private EV3MediumRegulatedMotor armMotor;
	
	//stores the location of the robot prior to navigating towards a located block
	//[x,y,theta]
	private double[] locationPreIdentifier; 

	/**
	 *Constructor
	 *@param objectID the colorID value of the target flag
	 *@param navigator contains methods which navigates the robot 
	 *@param odometer keeps track of the robot's position 
	 *@param usm access to the ultrasonic sensor 
	 *@param cd access to the light sensor. color detection feature. 
	 *@param robotArmMotor is the motor that controls the robot's arm
	 */
	public CaptureFlag(int objectID, Navigation navigator, Odometer odometer, UltrasonicModule usm, ColorDetection cd, EV3MediumRegulatedMotor robotArmMotor)
	{
		this.targetFlagColorID = objectID;
		this.nav = navigator; 
		this.odo = odometer;
		this.us = usm; 
		this.cd = cd;
		this.armMotor = robotArmMotor;
		
		locator = new LocateObject(nav, odo, us, this.cd);
		identifier = new IdentifyObject(us, this.cd);
		grabber = new PickupObject(armMotor);	
		
		this.locationPreIdentifier = null;
	}

	/**
	 *Overrides the run method in the Thread superclass
	 */
	@Override
	public void run()
	{
		//start looking for object (it's initialized to active and not paused)
		locator.start();
		
		//start identifier. (it's initialized to active but paused)
		identifier.start();
	}
	
	/**
	 *Method required by the IObserver interface.
	 *LocateObject and IdentifyObject call this method in order to notify CaptureFlag of an event.
	 *LocateObject ID = 1. IdentifyObject ID = 2.
	 *When LocateObject (1) finds a new object, it calls this method.
	 *When IdentifyObject (2) successfully identifies an object, it calls this method
	 *This method then reacts to the event.
	 *@param x caller class unique identifier
	 *@throws InvalidCallerID if update is called with an invalid class caller ID
	 */
	public void update(ClassID x) 
	{
		//switch on the caller class ID
		switch (x)
		{
		
			//caller is LocateObject 
			case LOCATEOBJECT:
				
				//get the location of the found object
				double[] objectLocation = locator.getCurrentObjLoco().clone();
				
				//if the location is not null (just being careful...)
				if(objectLocation!=null)
				{
					//pause locator
					locator.pauseThread();
					
					//save the current locaiton of the robot
					locationPreIdentifier = odo.getPosition();
					
					//navigate towards object
					nav.travelTo(objectLocation[0], objectLocation[1]);
					
					//identify object
					identifier.resumeThread();
				}
								
				break;
				
			//caller is IdentifyObject 
			case IDENTIFYOBJECT:
				int objectColorID = identifier.getObjectID();
				
				//if the object is the target flag
				if(objectColorID == targetFlagColorID)
				{
					//end identifier
					identifier.deactivateThread();
					
					//end locator
					locator.deactivateThread();
					
					//pickup flag
					grabber.doPickup();
				}
				else
				{
					//pause identifier
					identifier.pauseThread();
					
					//navigate back to where you were prior to navigating towards object (check for null location. just to be safe)
					if(locationPreIdentifier != null)
					{
						nav.travelTo(locationPreIdentifier[0], locationPreIdentifier[1]);
						nav.turnTo(locationPreIdentifier[2], true);
						
						//reset saved location for next itteration
						locationPreIdentifier = null;
					}
					
					//resume locator
					locator.resumeThread();
				}
												
				break;	
		}
	}
}