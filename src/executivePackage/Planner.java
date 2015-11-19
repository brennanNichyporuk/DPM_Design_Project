package executivePackage;

import captureFlagPackage.ClassID;
import basicPackage.IObserver;

/**
 * This class is responsible for coordinating all responsibilities of the robot.
 * @author brennanNichyporuk
 *
 */
public class Planner extends Thread implements IObserver {
	
	/**
	 * Instantiates several classes.
	 */
	public Planner() {
		
	}

	public void run() {
		
	}

	/**
	 * Called by observed classes to notify Planner of changes.
	 */
	
	public void update(ClassID x){
		
	}

}
