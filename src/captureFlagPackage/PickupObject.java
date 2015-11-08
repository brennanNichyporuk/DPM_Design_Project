package captureFlagPackage;

import basicPackage.Navigation;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
  
/**
 *A class which instructs the robot to pick up an object using its arm  
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class PickupObject 
{	
	/**
	 *Robot arm controller
	 */
	private ArmController armController; 
	
	//navigator 
	private Navigation nav;
	
	//length of the arm. 
	private final double armLength = 15.0;
	
	//how many degrees the arm motor should rotate
	private final int armMotorDegreesOfRotation = 490;
	
	/**
	 *Constructor 
	 *@param robotArmMotor the motor that controls the movement of the robot's arm
	 *@param navigator contains methods which navigates the robot 
	 */
	public PickupObject(EV3LargeRegulatedMotor aMotor, Navigation navigator)
	{
		this.nav = navigator;
		this.armController = new ArmController(aMotor, armMotorDegreesOfRotation);
	}
	
	/**
	 *This method controls the robot's arm in order to pick up the flag. 
 	 *It is called when the ID of any object found matches the one of the 
 	 *candidate object.
	 */
	void doPickup()
	{
		//move backwards to allow the arm to drop
		nav.moveStraight(-armLength);
		
		//drop arm
		armController.bringArmDown();
		
		//move forward to allow the arm grab the block
		nav.moveStraight(armLength);
		
		//raise arm
		armController.bringArmUp();
	}
}