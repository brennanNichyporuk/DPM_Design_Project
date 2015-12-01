package captureFlagPackage;

import basicPackage.*;
import lejos.hardware.Sound;
import modulePackage.ColorDetection;
import modulePackage.UltrasonicModule;

/**
 *A class which searches the environment in order to locate the target flag
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class LocateObject extends Thread
{
	//reference to the observer of this class
	private CaptureFlag captureFlag;
	
	private double[] objectLoco;
	
	private Navigation nav;	
	private Odometer odo;
	private UltrasonicModule us;
	private ColorDetection cd;
	
	//activity state variables
	private boolean isActive;
	private boolean isPaused;
	
	//Depending on the position of the object
	private boolean onSide = false;
	
	//stores the angle of the robot prior to starting this thread
	private final double originalRobotAngle;

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
		
		originalRobotAngle = odo.getAng();
	}
	
	/**
	 * returns true if the object detected is on the side of the robot. returns false if object is in front of the robot
	 * on side is defined as the object being located at an angle inferior to 20 or greater than 170 relative to the robot
	 * @return boolean whether or not the block is on the sides of the robot
	 */
	public boolean getOnSide()
	{
		return onSide;
	}
	
	/**
	 *Overrides the run method in the Thread superclass
	 */
	@Override
	public void run()
	{		
		while(isActive && (odo.getY()<(captureFlag.getInitialPostion()[1]+85)))
		{
			while(!isPaused && (odo.getY()<(captureFlag.getInitialPostion()[1]+85)))
			{
				scanArea();
			}
		}
	}
	
	/**
	 *Scans the area within a 180 degree radius in front of the robot.
	 *Records the position of the first object detected 
	 */
	private void scanArea()
	{					
		nav.turnTo(originalRobotAngle, true);
		
		//no objects detected. advance
		if(!sweep())
		{						
			nav.moveForward();
			try {Thread.sleep(nav.cm_to_seconds(10)*1000);} catch (InterruptedException e) {}	
			
			nav.stopMoving();
						
			nav.turnTo(originalRobotAngle, true);
		}
		//object detected. notify CaptureFlag
		else
		{
			captureFlag.update(ClassID.LOCATEOBJECT);
		}
	}
	
	
	//sweeps the ultrasonic sensor in order to detect objects and calculate their position
	private boolean sweep()
	{		
		us.rotateSensorToWait(-90.0);
				
		//how far the ultrasonic sensor should accept values
		final double clippingConstant = 30.0;
		
		//distance to block values (as read by us) and heading of the robot (as read by the odometer)
		double distance1=-1, distance2=-1, theta1=0, theta2=0;
						
		double previousDistance = clippingConstant;
						
		//turn 180 degrees
		while(us.getSensorAngle() < 90)
		{			
			double distance = rotateAndScan(us.getSensorAngle() + 10);	
			
			//get distance value from us
			double currentDistance = (distance<clippingConstant) ? distance : clippingConstant;
			
			//block found (rising edge)
			if(currentDistance<clippingConstant && (previousDistance-currentDistance > 0) && distance1<0)
			{
				distance1 = currentDistance;
				
				int angle = us.getSensorAngle();
				theta1 = (angle<0) ? (90-Math.abs(angle)) : (Math.abs(angle)+90);
				
				//block is angled positioned
				if(theta1 < 20 || theta1 > 70)
				{
					onSide = true;
				}
				else
				{
					onSide = false;
				}
				
				Sound.beep();
			}
			
			//block lost (falling edge)
			else if((currentDistance - previousDistance > 0) && distance1>0)
			{
				distance2 = currentDistance;
				
				int angle = us.getSensorAngle();
				theta2 = (angle<0) ? (90-Math.abs(angle)) : (Math.abs(angle)+90);
				
				Sound.beep();
								
				//calculate position of block and navigate towards it
				objectLoco = calculateObjectLocation(new double[]{distance1, distance2, theta1, theta2});
			
				us.rotateSensorToWait(0.0);
				return true;
			}
			
			//reset for next iteration
			previousDistance = currentDistance;
		}
		
		us.rotateSensorToWait(0.0);
		return false;
	}
	
	//rotates the ultrasonic sensor and fetches distance values
	private double rotateAndScan(int i) 
	{
		double distance;
		
		us.rotateSensorTo(i);
		this.sleepFor(25);
		
		distance = us.getDistance();
		this.sleepFor(25);
		
		distance = us.getDistance();
		this.sleepFor(25);
		
		distance = us.getDistance();
		
		return distance;
	}

	//makes the robot sleep for t seconds
	private void sleepFor(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	//calculates the location of the object
	private double[] calculateObjectLocation(double[] a)
	{
		double x_mid,y_mid;
		double distance1 = a[0],distance2 = a[1], theta1 = a[2], theta2 = a[3];
		
		//calculate relative x,y positions of each edge
		double[] loc1 = calculateObjectLocationHelper(distance1, theta1);
		double[] loc2 = calculateObjectLocationHelper(distance2, theta2);

		//calculate midpoint
		x_mid = (loc1[0] + loc2[0])/2;
		y_mid = Math.max(loc1[1], loc2[1]);
		
		return new double[] {x_mid, y_mid};
	}
	
	/**
	 * Calculates the x,y position of the corner of the block (relative to the robot), 
	 * based on the distance from the robot to the block and the heading of the robot at the time the edge was detected.
	 * @param distance
	 * @param theta1
	 * @return a double array of length 2 containing the x and y position
	 */
	private double[] calculateObjectLocationHelper(double distance, double theta1)
	{
		double x1,y1;
				
		if(theta1<90)
		{
			theta1 = 90-theta1;
			
			x1 = odo.getX() + (distance*Math.sin(Math.toRadians(theta1)));
			y1 = odo.getY() + (distance*Math.cos(Math.toRadians(theta1)));
		}
		else
		{
			theta1 = theta1-90;
			
			x1 = odo.getX() - (distance*Math.sin(Math.toRadians(theta1)));
			y1 = odo.getY() + (distance*Math.cos(Math.toRadians(theta1)));			
		}
					
		
		return new double[] {x1,y1};
	}
		
	/**
	 *Gets the location of the current object being analyzed.
	 *@return location of the current object found.
	 */
	double[] getCurrentObjLoco()
	{
		return objectLoco;
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
