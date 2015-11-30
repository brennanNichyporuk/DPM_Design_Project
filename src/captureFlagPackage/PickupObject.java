package captureFlagPackage;

import basicPackage.*;
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
		
	//how many degrees the arm motor should rotate
	private final int armMotorDegreesOfRotation = 620;
	
	private final CaptureFlag captureFlag;
	
	private final int armLength = 22;
	
	/**
	 *Constructor 
	 *@param robotArmMotor the motor that controls the movement of the robot's arm
	 *@param navigator contains methods which navigates the robot 
	 */
	public PickupObject(EV3LargeRegulatedMotor aMotor, Navigation navigator, CaptureFlag captureFlag)
	{
		this.nav = navigator;
		this.armController = new ArmController(aMotor, armMotorDegreesOfRotation);
		this.captureFlag = captureFlag;
	}
	
	/**
	 *This method controls the robot's arm in order to pick up the flag. 
 	 *It is called when the ID of any object found matches the one of the 
 	 *candidate object.
	 */
	void doPickup()
	{
		nav.moveBackward();
		try {Thread.sleep(nav.cm_to_seconds(armLength)*1000);} catch (InterruptedException e) {}
		nav.stopMoving();
		
		//drop arm
		armController.bringArmDown();
		
		//move forward 14cm
		nav.moveForward();
		try {Thread.sleep(nav.cm_to_seconds(armLength)*1000);} catch (InterruptedException e) {}
		nav.stopMoving();

		//bring arm up (capture block)
		armController.bringArmUp();		
	}
	
	/**
	 *Dumps the block to a designated drop off zone 
	 */
	void discardBlock()
	{
		//navigate to drop zone 
		nav.travelTo(captureFlag.getLocationPreIdentifier()[0], captureFlag.getLocationPreIdentifier()[1]);
		
		nav.moveForward();
		try {Thread.sleep(nav.cm_to_seconds(10)*1000);} catch (InterruptedException e) {}
		nav.stopMoving();
		
		//bring arm down (release block)
		armController.bringArmDown(450);
		
		//backing up in order to leave the block in the position
		nav.moveBackward();
		try {Thread.sleep(nav.cm_to_seconds(armLength-5)*1000);} catch (InterruptedException e) {}
		nav.stopMoving();
		
		//bring arm back up
		armController.bringArmUp(450);
	}
}