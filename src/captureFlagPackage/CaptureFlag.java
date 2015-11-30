package captureFlagPackage;

import basicPackage.*;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import modulePackage.*;

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
		
	//stores the location of the robot prior to navigating towards a located block
	//[x,y,theta]
	private double[] locationPreIdentifier; 
	
	private double[] initialPosition;
	

	/**
	 *Constructor
	 *@param objectID the colorID value of the target flag
	 *@param navigator contains methods which navigates the robot 
	 *@param odometer keeps track of the robot's position 
	 *@param usm access to the ultrasonic sensor 
	 *@param cd access to the light sensor. color detection feature. 
	 *@param robotArmMotor is the motor that controls the robot's arm
	 */
	public CaptureFlag(int objectID, Navigation navigator, Odometer odometer, UltrasonicModule usm, ColorDetection cd, EV3LargeRegulatedMotor robotArmMotor)
	{
		this.targetFlagColorID = objectID;
		this.nav = navigator; 
		this.odo = odometer;
		this.us = usm; 
		this.cd = cd;
		
		locator = new LocateObject(this, nav, odo, us, this.cd);
		identifier = new IdentifyObject(this, us, this.cd);
		grabber = new PickupObject(robotArmMotor, navigator, this);	
		
		this.locationPreIdentifier = null;
		this.initialPosition = odo.getPosition();
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
				try {Thread.sleep(100);} catch (InterruptedException e) {}
				
				//get the location of the found object
				double[] objectsLocation = locator.getCurrentObjLoco();
				
				//if the location is not null (just being careful...)
				if(objectsLocation!=null)
				{
					//pause locator
					locator.pauseThread();
					
					//save the current location of the robot
					locationPreIdentifier = odo.getPosition();
										
					//navigate towards object
					//If the object is not detected on the side, travel to y, then to x
					if(!locator.getOnSide())
					{
						nav.travelTo(objectsLocation[0], odo.getY());	
						nav.travelTo(odo.getX(), objectsLocation[1]);	
					}
					//Else, travel to x, then to y
					else
					{
						nav.travelTo(odo.getX(), objectsLocation[1]);	
						nav.travelTo(objectsLocation[0], odo.getY());
					}

					while (Button.waitForAnyPress() != Button.ID_ESCAPE);

					//identify object.
					identifier.resumeThread();					
				}
												
				break;
				
			//caller is IdentifyObject 
			case IDENTIFYOBJECT:
								
				int objectColorID = identifier.getObjectID();
				
				//if the object is the target flag
				if(objectColorID == targetFlagColorID)
				{
					identifier.pauseThread();
					locator.pauseThread();
					
					//end identifier
					identifier.deactivateThread();
					
					//end locator
					locator.deactivateThread();
					
					Sound.beep();Sound.beep();Sound.beep();
					grabber.doPickup();
				}
				else
				{
					//pause identifier
					identifier.pauseThread();
					
					//grabber.doPickup();
					//grabber.discardBlock();
					
					//navigate back to where you were prior to navigating towards object (check for null location. just to be safe)
					if(locationPreIdentifier != null)
					{
						nav.travelTo(odo.getX(), locationPreIdentifier[1]);
						nav.travelTo(locationPreIdentifier[1], odo.getY());
						nav.turnTo(90, true);
						
						//reset saved location for next iteration
						locationPreIdentifier = null;
					}
				
					locator.resumeThread();
				}
												
				break;	
		}
	}
	
	double[] getInitialPostion()
	{
		return this.initialPosition;
	}
	double[] getLocationPreIdentifier()
	{
		return this.locationPreIdentifier;
	}
}