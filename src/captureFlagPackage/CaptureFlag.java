package captureFlagPackage;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import modulePackage.*;
import basicPackage.*;

/**
 *A class which is responsible for locating and capturing the flag 
 *@author Fred Glozman & Abdel Kader Gaye 
 */
public class CaptureFlag extends Thread implements IObserver
{
	private final int targetFlagColorID; 
	
	private LocateObject locator;
	private IdentifyObject identifier;
	private PickupObject grabber;  
	
	private Navigation nav;	
	private Odometer odo;
	private UltrasonicModule us; 
	private ColorDetection cd; 
	
	private EV3MediumRegulatedMotor armMotor;

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
		identifier = new IdentifyObject(targetFlagColorID, us, this.cd);
		grabber = new PickupObject(armMotor);
	}

	/**
	 *Overrides the run method in the Thread superclass
	 */
	@Override
	public void run()
	{

	}

	/**
	 *Method required by the IObserver interface.
 	 *This method makes the CaptureFlag class retrieve the ID of an object.
 	 *If it matches the ID of the candidate object, it will call doPickup() 
 	 *from PickupObject class. Else if it doesn't, the search will continue.
	 *@param x ID of any object found 
	 */
	public void update(int x)
	{

	}
}