package mapPackage;

import java.util.Arrays;

import pilotPackage.DStarLite;
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
	private DStarLite dStarLite;
	private int sensorAxleOffset;
	
	private int detectionThreshold = 38;
	private int scanBand = 25;
	private int scanIncrement = 25;

	/**
	 * Instantiates a map.
	 * @param odo - reference to the Odometer thread.
	 * @param uM - reference to an instance of the UltrasonicModule
	 */
	public Mapper(Odometer odo, UltrasonicModule uM, DStarLite dStarLite, int sensorAxleOffset) {
		this.odo = odo;
		this.uM = uM;
		this.dStarLite = dStarLite;
		this.sensorAxleOffset = sensorAxleOffset;
		this.cycleUSsensor();
	}

	/**
	 * This method uses the ULtrasonic sensor to scan its environment and update 
	 * the favorability of each node based on the presence or absence of obstacles.
	 * @param x - present node x
	 * @param y - present node y
	 * @return - updated map.
	 */
	public boolean updateAndReturnMap() {
		return this.scan();
	}

	private boolean scan() {

		double distance;
		double ABSAngle = 0;
		boolean objectDetected = false;

		for (int i = -scanBand; i <= scanBand; i += scanIncrement) {
			uM.rotateSensorTo(i);
			distance = this.cycleUSsensor();
			if (distance < detectionThreshold) {
				ABSAngle = this.wrapDatAngle(i + odo.getAng());
				try {
					int[] nodeID = this.locateDatObjectNode(ABSAngle, distance);
					dStarLite.updateCell(nodeID[0], nodeID[1], -1);
					objectDetected = true; // Skipped if there is an Exception
				} catch (FalseObjectException fOE) {
					System.out.println("FalseObjectE");
				}
			}
		}
		return objectDetected;
	}

	private double wrapDatAngle(double angle) {
		if (angle < 0)
			angle += 360;
		else if (angle > 360)
			angle -= 360;
		return angle;
	}

	private int[] locateDatObjectNode(double angle, double distance) throws FalseObjectException {
		double sensorX = odo.getX() + this.sensorAxleOffset * Math.cos(Math.toRadians(odo.getAng()));
		double sensorY = odo.getY() + this.sensorAxleOffset * Math.sin(Math.toRadians(odo.getAng()));
		
		double objectX = sensorX + distance * Math.cos(Math.toRadians(angle));
		double objectY = sensorY + distance * Math.sin(Math.toRadians(angle));
		
		if (objectX < 10 || objectY < 10)
			throw new FalseObjectException();

		int nodeX = (int) (objectX / 30.48);
		int nodeY = (int) (objectY / 30.48);
		int[] nodeID = {nodeX, nodeY};

		int currentNodeX = (int) (odo.getX() / 30.48);
		int currentNodeY = (int) (odo.getY() / 30.48);

		// If the object is off the map or the object detected is the wall, raise FalseObjectException.
		if (nodeX > 11 || nodeY > 11 || nodeX < 0 || nodeY < 0 || (nodeX == currentNodeX && nodeY == currentNodeY)) {
			throw new FalseObjectException();
		}

		// If the object is not on an adjacent node, ignore...
		if (!((nodeX == currentNodeX + 1 && nodeY == currentNodeY) || (nodeX == currentNodeX - 1 && nodeY == currentNodeY)
				|| (nodeX == currentNodeX && nodeY == currentNodeY + 1) || (nodeX == currentNodeX && nodeY == currentNodeY - 1)))
			throw new FalseObjectException();
		
		System.out.println("Ox:" + (int) objectX + "Oy:" + (int) objectY);

		return nodeID;
	}
	
	private int cycleUSsensor() {
		
		int distance = 0;
		for (int i = 0; i < 5; i++) {
			distance = uM.getDistance();
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return distance;
	}

}
