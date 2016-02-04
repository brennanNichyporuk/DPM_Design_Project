package captureFlagPackage;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * ArmController.java
 * This class was created in order to control the arm of the robot.
 * NOTE: Assuming that robotic arm is initially raised.
 * @author Fred Glozman, Abdel Kader Gaye
 */
public class ArmController {
	
	//robot arm motor
	private EV3LargeRegulatedMotor aMotor;
	
	//true if the arm is already up
	private boolean isUp = true;
	
	//how many degrees the motor should rotate
	private int degreesOfRotation;
	
	/**
	 * Constructor which takes a motor and degrees or rotation
	 * @param aMotor motor which controls the 
	 * @param degreesOfRotation
	 */
	public ArmController(EV3LargeRegulatedMotor aMotor, int degreesOfRotation){
		this.aMotor = aMotor;
		this.degreesOfRotation = degreesOfRotation;
		
		this.aMotor.setAcceleration(1000);
		this.aMotor.setSpeed(200);
	}
	
	
	/**
	 * Lowers the robotic arm
	 */
	public void bringArmDown(){
		if(isUp){
			aMotor.rotate(-degreesOfRotation);
			isUp = false;
		}
	}
	
	/**
	 * Raises the robotic arm
	 */	public void bringArmUp(){
		if(!isUp){
			aMotor.rotate(degreesOfRotation);
			isUp = true;
		}
	}
	 
	/**
	 * Lowers the robotic arm
	 */
	public void bringArmDown(int x){
		if(isUp){
			aMotor.rotate(-x);
			isUp = false;
		}
	}
	
	/**
	 * Raises the robotic arm
	 */	
	public void bringArmUp(int x){
		if(!isUp){
			aMotor.rotate(x);
			isUp = true;
		}
	}
}
