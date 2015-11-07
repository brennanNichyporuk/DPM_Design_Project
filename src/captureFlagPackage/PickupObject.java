package captureFlagPackage;

import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 *A class which instructs the robot to pick up an object using its arm  
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class PickupObject 
{	
	/**
	 *Medium motor which controls the robot's arm
	 */
	private EV3MediumRegulatedMotor armMotor;

	/**
	 *Constructor 
	 *@param robotArmMotor the motor that controls the movement of the robot's arm
	 */
	public PickupObject(EV3MediumRegulatedMotor robotArmMotor)
	{
		this.armMotor = robotArmMotor;
	}
	
	/**
	 *This method controls the robot's arm in order to pick up the flag. 
 	 *It is called when the ID of any object found matches the one of the 
 	 *candidate object.
	 */
	void doPickup()
	{

	}
}