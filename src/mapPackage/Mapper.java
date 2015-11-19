package mapPackage;

import java.util.Arrays;

import pilotPackage.DStarLite;
import basicPackage.Odometer;
import lejos.hardware.Sound;
import modulePackage.UltrasonicModule;

/**
 * This class is responsible for generating the map used by the Pilot Thread to 
 * navigate to its destination.
 * @author brennanNichyporuk
 *
 */
public class Mapper extends Thread {
	private Object lock1 = new Object();

	private Odometer odo;
	private UltrasonicModule uM;
	private DStarLite dStarLite;
	private int sensorAxleOffset;

	private final int maxRange = 65;
	private final int minDerivativeChange = 5;
	private int scanBand = 100;
	private final int scanIncrement = 5;

	private boolean active;
	private boolean objectDetected;

	/**
	 * Instantiates a map.
	 * @param odo - reference to the Odometer thread.
	 * @param uM - reference to an instance of the UltrasonicModule
	 */
	public Mapper(Odometer odo, UltrasonicModule uM, /*DStarLite dStarLite */ int sensorAxleOffset) {
		this.odo = odo;
		this.uM = uM;
		//this.dStarLite = dStarLite;
		this.sensorAxleOffset = sensorAxleOffset;
		this.active = false;
		this.objectDetected = false;
		this.cycleUSsensor(5);

		/*
		for (int i = 0; i < this.mapSize; i++)
			dStarLite.updateCell(i, this.mapSize, -1);

		dStarLite.updateCell(this.mapSize, this.mapSize, -1);

		for (int j = 0; j < this.mapSize; j++)
			dStarLite.updateCell(this.mapSize, j, -1);
		 */
	}

	/**
	 * This method uses the ULtrasonic sensor to scan its environment and update 
	 * the favorability of each node based on the presence or absence of obstacles.
	 * @param x - present node x
	 * @param y - present node y
	 * @return - updated map.
	 */

	public void run() {
		while (true) {
			if (active) {
				this.objectDetected = this.scan2();
				active = false;
			}
			else
				this.sleepFor(250);
		}
	}

	private boolean scan2() {
		double distance = 0, lastDistance = 0, fallingEdgeDistance = 0;
		int[] objectFallingEdgeLoco = null, objectRisingEdgeLoco = null;
		int lastABSAngle = 0, fallingEdgeABSAngle = 0;
		boolean objectDetected = false, fallingEdgeDetected = false, fallingEdgeRegistered = false;

		uM.rotateSensorTo(-scanBand);
		lastDistance = this.cycleUSsensor(5);
		lastABSAngle = -scanBand;

		while (true) {
			for (int i = -scanBand; i <= scanBand; i += scanIncrement) {
				uM.rotateSensorTo(i);
				distance = this.cycleUSsensor(5);
				double derivative = distance - lastDistance;
				int ABSAngle = (int) this.wrapDatAngle(i + odo.getAng());

				// If the ABS of the derivative is great enough ...
				if (Math.abs(derivative) > this.minDerivativeChange) {
					// If the derivative is negative --> falling edge
					if (derivative < 0) {
						fallingEdgeDetected = true;
					}
					// If the derivative is positive --> rising edge
					else if (derivative > 0 && fallingEdgeRegistered) {
						if (lastDistance < this.maxRange) {
							objectFallingEdgeLoco = this.locateDatObjectEdge(fallingEdgeDistance, fallingEdgeABSAngle);
							objectRisingEdgeLoco = this.locateDatObjectEdge(lastDistance, lastABSAngle);
							
							try {
								this.updatePath(objectFallingEdgeLoco, objectRisingEdgeLoco);
							} catch (FalseObjectException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							fallingEdgeRegistered = false;
						}
					}

				} 
				else {

					if (fallingEdgeDetected) {
						fallingEdgeDistance = distance;
						fallingEdgeABSAngle = ABSAngle;
						fallingEdgeDetected = false;
						fallingEdgeRegistered = true;
					}

				}

				lastABSAngle = ABSAngle;
				lastDistance = distance;
			}
		}

	}

	private double wrapDatAngle(double angle) {
		if (angle < 0)
			angle += 360;
		else if (angle > 360)
			angle -= 360;
		return angle;
	}

	private void updatePath(int[] objectFallingEdgeLoco, int[] objectRisingEdgeLoco) throws FalseObjectException {

		double[] objectCenterLoco = {(objectFallingEdgeLoco[0] + objectRisingEdgeLoco[0]) / 2, (objectFallingEdgeLoco[1] + objectRisingEdgeLoco[1]) / 2};
		int[] objectCenterNode = {(int) (objectCenterLoco[0] / 30.48), (int) (objectCenterLoco[1] / 30.48)};
		System.out.println(Arrays.toString(objectCenterNode));
		Sound.beep();
		this.sleepFor(5000);
	}


	private int[] locateDatObjectEdge(double distance, double angle) {
		double[] sensorLoco = this.locateDatSensorLoco();
		int objectX = (int) (sensorLoco[0] + distance * Math.cos(Math.toRadians(angle)));
		int objectY = (int) (sensorLoco[1] + distance * Math.sin(Math.toRadians(angle)));
		int[] objectLoco = {objectX, objectY};
		return objectLoco;
	}

	private double[] locateDatSensorLoco() {
		double sensorX = odo.getX() + this.sensorAxleOffset * Math.cos(Math.toRadians(odo.getAng()));
		double sensorY = odo.getY() + this.sensorAxleOffset * Math.sin(Math.toRadians(odo.getAng()));
		double[] sensorLoco = {sensorX, sensorY};
		return sensorLoco;
	}

	private double cycleUSsensor(int cycleCount) {

		long startTime;
		double distance = 0;
		for (int i = 0; i < cycleCount; i++) {
			startTime = System.currentTimeMillis();
			distance = uM.getDistance();
			if (System.currentTimeMillis() - startTime < 25)
				this.sleepFor(25);
		}

		return distance;

	}

	private void sleepFor(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isObjectDetected() {
		return objectDetected;
	}

	public void setObjectDetected(boolean objectDetected) {
		this.objectDetected = objectDetected;
	}

}
