package captureFlagPackage;

import modulePackage.*;


/**
 *A class which uses the robot's sensors to identify an object.
 *This class distinguishes between the target block and all the other objects.
 *@author Fred Glozman, Abdel Kader Gaye
 */
public class IdentifyObject extends Thread
{	
	/**
	 * a value of -1 signifies that the light sensor failed to get a reading.
	 * acceptable values are in the range [0, 13].
	 */
	private int objectID;
	
	//reference to the observer of this class
	private CaptureFlag captureFlag;
	
	private UltrasonicModule us; 
	private ColorDetection cd; 
	
	//activity state variables
	private boolean isActive;
	private boolean isPaused;
	
	private final double[] blocks = 
	{	-1, //nothing
		6, //light blue
		0, //red
		3, //yellow
		6, //white
		2 //dark blue
	};


	/**
	 *Constructor. Requires user to specify colorID value of the target flag
	 *@param usm access to the ultrasonic sensor 
	 *@param cd access to the light sensor. color detection feature. 
	 */
	public IdentifyObject(CaptureFlag captureFlag, UltrasonicModule usm, ColorDetection cd)
	{
		this.captureFlag = captureFlag;
		this.us = usm; 
		this.cd = cd;
		
		//initialize objectID (CHANGE THIS TO -1)
		objectID = -1;
		
		//initialized to active and paused (i.e. thread will be idle)
		this.isActive = true;
		this.isPaused = true;
	}
	
	private int isBlock(int colorID)
	{
		for(int i=1; i<6; i++)
		{
			if(colorID == blocks[i])
			{
				return i;
			}
		}
		return -1;
	}
	

	/**
	 *Overrides the run method in the Thread superclass
	 */
	@Override
	public void run()
	{
		while(isActive)
		{				
			
			boolean didRead = false;
			boolean justStartedRotating = true;
			while(!isPaused)
			{	
				if(didRead)
				{
					if(justStartedRotating)
					{
						us.rotateSensorToWait(-90);
						justStartedRotating = false;
					}
					else
					{
						us.rotateSensorTo(us.getSensorAngle()+10);
						
						if(us.getSensorAngle()==90)
						{
							captureFlag.update(ClassID.IDENTIFYOBJECT);
							justStartedRotating = true;
							us.rotateSensorToWait(0);
							continue;
						}
					}
				}
				
				//allow sensor to stabilize before getting next color reading
				try {Thread.sleep(100);} catch (InterruptedException e1) {}

				//get color reading
				objectID = isBlock(cd.getData());
				didRead = true;
				
				if(objectID!=-1)
				{
					//notify CaptureFlag class
					us.rotateSensorToWait(0.0);
					didRead = false;
					captureFlag.update(ClassID.IDENTIFYOBJECT);
					try {Thread.sleep(500);} catch (InterruptedException e){}
				}
				
				try {Thread.sleep(500);} catch (InterruptedException e){}
			}
			try {Thread.sleep(500);} catch (InterruptedException e){}
		}
	}
	
	
	
	/**
	 *This method utilizes the robot's light sensor in order to
	 *determine the colorID value of the object directly in front of the light sensor
	 *@return returns the colorID value of the object being analyzed
	 */
	int getObjectID()
	{
		return objectID;
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
