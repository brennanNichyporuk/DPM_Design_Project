package pilotPackage;

import java.util.List;

import captureFlagPackage.ClassID;
import mapPackage.Mapper;
import modulePackage.UltrasonicModule;
import basicPackage.IObserver;
import basicPackage.Navigation;
import basicPackage.Odometer;

/**
 * This class is is responsible for navigating to the opponents home zone while
 * avoiding obstacles.
 * @author brennanNichyporuk
 *
 */
public class Pilot extends Thread {
	private IObserver observer;
	private Navigation navigation;
	private Odometer odometer;
	private DStarLite dStarLite;
	private Mapper mapper;
	private List<pilotPackage.State> path;

	/**
	 * Instantiates an instance of Mapper.
	 * @param observer - reference to Planner Thread
	 * @param nav - reference to instance of Navigation 
	 * @param odo - reference to Odometer Thread
	 * @param uM - reference to instance of Ultrasonic Module
	 * @param startX - start nodeX
	 * @param startY - start nodeY
	 * @param goalX - goal nodeX
	 * @param goalY - goal nodeY
	 */
	public Pilot(IObserver observer, Navigation nav, Odometer odo, UltrasonicModule uM, 
			int startX, int startY, int goalX, int goalY) {

		this.observer = observer;
		this.navigation = nav;
		this.odometer = odo;
		this.dStarLite = new DStarLite();
		this.dStarLite.init(startX, startY, goalX, goalY);
		this.dStarLite.replan();
		this.mapper = new Mapper(odo, uM, dStarLite);
		this.path = this.dStarLite.getPath();
	}

	public void run() {
		while (this.path.size() > 1) {
			int currentNodeX = (int) (this.odometer.getX() / 30.48);
			int currentNodeY = (int) (this.odometer.getY() / 30.48);
			this.dStarLite.updateStart(currentNodeX, currentNodeY);
			this.dStarLite.replan();

			pilotPackage.State currentState = this.path.get(0);
			pilotPackage.State nextState = this.path.get(1);
			int deltaX = nextState.x - currentState.x;
			int deltaY = nextState.y - currentState.y;
			this.faceNextBlock(deltaX, deltaY);
			
			this.mapper.scan();
			nextState = this.path.get(1);
			this.travelToNode(nextState);
		}
		this.notifyObserver();
	}

	/**
	 * This method takes as input an 'x' and 'y' that correspond to a goal
	 * location on the map. This method uses information from the 'map' variable of
	 * the Map class in order to effectively pilot the robot to its destination 
	 * while avoiding obstacles.
	 * @param x
	 * @param y
	 */
	public void pilotTo(int x, int y) {
		this.dStarLite.updateGoal(x, y);
		this.dStarLite.replan();
	}

	private void faceNextBlock(int deltaX, int deltaY) {
		if (deltaX == 1 && deltaY == 0) {
			this.navigation.turnTo(0, true);
		}
		else if (deltaX == 1 && deltaY == 1) {
			this.navigation.turnTo(45, true);			
		}
		else if (deltaX == 0 && deltaY == 1) {
			this.navigation.turnTo(90, true);
		}
		else if (deltaX == -1 && deltaY == 1) {
			this.navigation.turnTo(135, true);
		}
		else if (deltaX == -1 && deltaY == 0) {
			this.navigation.turnTo(180, true);
		}
		else if (deltaX == -1 && deltaY == -1) {
			this.navigation.turnTo(225, true);
		}
		else if (deltaX == 0 && deltaY == -1) {
			this.navigation.turnTo(270, true);
		}
		else if (deltaX == 1 && deltaY == -1) {
			this.navigation.turnTo(315, true);
		}
		
	}

	private void travelToNode(pilotPackage.State nextState) {
		int nodeX = nextState.x;
		int nodeY = nextState.y;
		double actualX = nodeX * 30.48 + 15.24;
		double actualY = nodeY * 30.48 + 15.24;

		this.navigation.travelTo(actualX, actualY);
	}
	
	private void notifyObserver() {
		this.observer.update(ClassID.PILOT);
	}
}
