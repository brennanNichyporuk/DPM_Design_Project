package mapPackage;

import basicPackage.Odometer;
import modulePackage.UltrasonicModule;

/**
 * This class is responsible for generating the map used by the Pilot Thread to 
 * navigate to its destination.
 * @author brennanNichyporuk
 *
 */
public class Mapper {
	private Odometer odo;
	private UltrasonicModule uM;
	private Node[][] map;
	
	/**
	 * Instantiates a map.
	 * @param odo - reference to the Odometer thread.
	 * @param uM - reference to an instance of the UltrasonicModule
	 */
	public Mapper(Odometer odo, UltrasonicModule uM) {
		this.odo = odo;
		this.uM = uM;
		
		this.map = new Node[12][12];
	}
	
	/**
	 * This method uses the ULtrasonic sensor to scan its environment and update 
	 * the favorability of each node based on the presence or absence of obstacles.
	 * @return - updated map.
	 */
	public Node[][] updateAndReturnMap() {
		
		return map;
	}
	
}
