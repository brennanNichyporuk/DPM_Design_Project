package pilotPackage;

import mapPackage.Mapper;
import modulePackage.UltrasonicModule;
import basicPackage.IObserver;
import basicPackage.Navigation;
import basicPackage.Odometer;

public class Pilot extends Thread {
	private IObserver observer;
	private Navigation nav;
	private Mapper mapper;
	
	private double goalX;
	private double goalY;
	
	/**
	 * Instantiates an instance of Mapper.
	 * @param observer
	 * @param nav
	 * @param odo
	 * @param uM
	 */
	public Pilot(IObserver observer, Navigation nav, Odometer odo, UltrasonicModule uM) {
		this.observer = observer;
		this.nav = nav;
		this.mapper = new Mapper(odo, uM);
	}
	
	public void run() {
		
	}
	
	/**
	 * This method takes as input an 'x' and 'y' that correspond to a goal
	 * location on the map. This method uses information from the 'map' variable of
	 * the Map class in order to effectively pilot the robot to its destination 
	 * while avoiding obstacles.
	 * @param x
	 * @param y
	 */
	public void pilotTo(double x, double y) {
		this.goalX = x;
		this.goalY = y;
	}
	
	
}
