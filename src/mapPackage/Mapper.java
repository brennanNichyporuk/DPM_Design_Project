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

	private int detectionThreshold = 5;
	private int scanBand = 30;
	private int scanIncrement = 5;

	/**
	 * Instantiates a map.
	 * @param odo - reference to the Odometer thread.
	 * @param uM - reference to an instance of the UltrasonicModule
	 */
	public Mapper(Odometer odo, UltrasonicModule uM, DStarLite dStarLite) {
		this.odo = odo;
		this.uM = uM;
		this.dStarLite = dStarLite;
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
		double lastDistance = uM.getDistance();
		double edgeABSAngle = 0;
		double edgeDistance = 0;
		boolean objectDetected = false;
		boolean detectionThresholdExceeded = false;

		for (int i = -scanBand; i < scanBand; i += scanIncrement) {
			uM.rotateSensorTo(i);
			distance = uM.getDistance();

			if (Math.abs(distance - lastDistance) >= detectionThreshold) { // Distance Change
				edgeABSAngle = this.wrapDatAngle(i + odo.getAng());
				edgeDistance = distance;
				detectionThresholdExceeded = true;
			}
			else if (detectionThresholdExceeded) {
				try {
					int[] nodeID = this.locateDatObjectNode(edgeABSAngle, edgeDistance);
					dStarLite.updateCell(nodeID[0], nodeID[1], -1);
					objectDetected = true;
				} catch (FalseObjectException fOE) {

				}
				detectionThresholdExceeded = false;
			}
			lastDistance = distance;
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

	private int[] locateDatObjectNode(double edgeABSAngle, double edgeDistance) throws FalseObjectException {
		double objectX = odo.getX() + edgeDistance * Math.cos(Math.toRadians(edgeABSAngle));
		double objectY = odo.getY() + edgeDistance * Math.sin(Math.toRadians(edgeABSAngle));

		int nodeX = (int) (objectX / 30.48);
		int nodeY = (int) (objectY / 30.48);
		int[] nodeID = {nodeX, nodeY};
		
		int currentNodeX = (int) (odo.getX() / 30.48);
		int currentNodeY = (int) (odo.getY() / 30.48);

		if (nodeX > 11 || nodeY > 11 || nodeX < 0 || nodeY < 0 || (nodeX == currentNodeX && nodeY == currentNodeY)) {
			throw new FalseObjectException();
		}

		return nodeID;
	}

}
