package mapPackage;

import basicPackage.Odometer;
import modulePackage.UltrasonicModule;

public class Mapper {
	private Odometer odo;
	private UltrasonicModule uM;
	private Node[][] map;
	
	/**
	 * 
	 * @param odo
	 * @param uM
	 */
	public Mapper(Odometer odo, UltrasonicModule uM) {
		this.odo = odo;
		this.uM = uM;
		
		this.map = new Node[12][12];
	}
	
	/**
	 * This method uses the ULtrasonic sensor to scan its environment and update 
	 * the favorability of each node based on the presence or absence of obstacles.
	 * @return
	 */
	public Node[][] updateAndReturnMap() {
		
		return map;
	}
	
}
