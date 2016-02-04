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
	private Odometer odo;
	private UltrasonicModule uM;
	private DStarLite dStarLite;
	private double sensorAxleOffset = 5.5;
	private double sensorRotationOffset = 3.0;

	private final int maxRange = 50;
	private final int minDerivativeChange = 6;
	private int scanBand = 90;
	private final int scanIncrement = 5;
	private final int mapSize = 12;
	private boolean active;
	private boolean objectDetected;
	private boolean odd;

	// BACKUP: 22, 8
	// BACKUP2: 18, 12
	private int risingEdgeABSAngleCorrectionFactor = 26, fallingEdgeABSAngleCorrectionFactor = 20;

	private long lastGetDistanceTime;

	/**
	 * Instantiates a map.
	 * @param odo - reference to the Odometer thread.
	 * @param uM - reference to an instance of the UltrasonicModule
	 */
	public Mapper(Odometer odo, UltrasonicModule uM, DStarLite dStarLite) {

		this.odo = odo;
		this.uM = uM;
		this.dStarLite = dStarLite;
		this.active = false;
		this.objectDetected = false;
		this.cycleUSsensor(5);
		this.odd = true;
		this.lastGetDistanceTime = System.currentTimeMillis();

		uM.rotateSensorToWait(-scanBand);

		for (int i = 1; i < this.mapSize; i++) {
			dStarLite.updateCell(i, 0, -1);
			dStarLite.updateCell(0, i, -1);
		}

		for (int i = 0; i < this.mapSize; i++) {
			dStarLite.updateCell(i, this.mapSize - 1, -1);
			dStarLite.updateCell(this.mapSize - 1, i, -1);
		}

		dStarLite.replan();
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
				this.objectDetected = this.scan();
				this.active = false;
			}
			else
				this.sleepFor(250);
		}
	}

	public boolean scan() {
		double distance = 0, lastDistance = 0, fallingEdgeDistance = 0;
		double[] objectFallingEdgeLoco = null, objectRisingEdgeLoco = null, objectLoco = null;
		int lastAngle = 0, fallingEdgeAngle = 0;
		boolean objectDetected = false, fallingEdgeDetected = false, fallingEdgeRegistered = false;

		lastDistance = this.cycleUSsensor(5);
		int i;
		if (odd)
			i = -scanBand;
		else
			i = scanBand;

		lastAngle = i;

		while (Math.abs(i) <= Math.abs(scanBand)) {
			distance = this.rotateAndScan(i);
			double derivative = distance - lastDistance;
			//int ABSAngle = (int) this.wrapDatAngle(i + odo.getAng());
			// If the ABS of the derivative is great enough ...
			if (Math.abs(derivative) > this.minDerivativeChange) {
				// If the derivative is negative --> falling edge
				if (derivative < 0) {
					fallingEdgeDetected = true;
				}
				// If the derivative is positive --> rising edge
				// If there is a rising edge and a fallingEdge has been registered, there must have been an object ...
				else if (derivative > 0 && fallingEdgeRegistered) {
					// If the Object is within max range ...
					if (lastDistance < this.maxRange) {
						// Locate the falling and rising edge location ...
						// Note that 15 is added or subtracted since the Ultrasonic sensor detects distance
						// + or - 15 degrees from the direction it is pointing.
						try {
							// Corrected angles ...
							int correctedFallingEdgeAngle = this.correctedEdgeAngle(fallingEdgeAngle, true);
							int correctedRisingEdgeAngle = this.correctedEdgeAngle(lastAngle, false);

							// CORRECT RISING EDGE DISCREPENCY AS DIRECTED TO BY EDGE DETECTION TESTING
							lastDistance -= 3.75;

							// CORRECT FALLING EDGE DISCREPENCY
							fallingEdgeDistance -= 3;

							// Locate edges
							objectFallingEdgeLoco = this.locateDatObjectEdge(fallingEdgeDistance, correctedFallingEdgeAngle, fallingEdgeAngle);
							objectRisingEdgeLoco = this.locateDatObjectEdge(lastDistance, correctedRisingEdgeAngle, lastAngle);

							// Update the path ...
							this.updatePath(objectFallingEdgeLoco, objectRisingEdgeLoco);

							objectDetected = true;
						} catch (FalseObjectException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						fallingEdgeRegistered = false;
					}
				}
				// If only risingEdge (no falling edge)
				else if (lastDistance < this.maxRange){
					try {
						// Find risingEdge ...
						int correctedRisingEdgeAngle = this.correctedEdgeAngle(lastAngle, false);
						objectRisingEdgeLoco = this.locateDatObjectEdge(fallingEdgeDistance, correctedRisingEdgeAngle, lastAngle);

						// Use the square on the right (left) scan range as default start ...
						if (odd)
							objectFallingEdgeLoco = this.locateDatObjectEdge(32, -55, -55);
						else
							objectFallingEdgeLoco = this.locateDatObjectEdge(32, 55, 55);

						this.updatePath(objectFallingEdgeLoco, objectRisingEdgeLoco);

					} catch (FalseObjectException e) {}
				}
			} 
			else {
				// This records the first distance and angle that does not meet the minimum derivative
				// provided the a falling edge was previously detected.
				if (fallingEdgeDetected) {
					fallingEdgeDistance = distance;
					fallingEdgeAngle = i;
					fallingEdgeDetected = false;
					fallingEdgeRegistered = true;
				}
			}
			lastAngle = i;
			lastDistance = distance;

			if (odd)
				i += scanIncrement;
			else
				i -= scanIncrement;
		}
		// If only fallingEdge (no rising edge)
		if (fallingEdgeRegistered && fallingEdgeDistance < this.maxRange) {
			try {
				// Find fallingEdge ...
				int correctedFallingEdgeAngle = this.correctedEdgeAngle(fallingEdgeAngle, true);
				objectFallingEdgeLoco = this.locateDatObjectEdge(fallingEdgeDistance, correctedFallingEdgeAngle, fallingEdgeAngle);

				// Use the square on the right (left) scan range as default start ...
				if (odd)
					objectRisingEdgeLoco = this.locateDatObjectEdge(32, 55, 55);
				else
					objectFallingEdgeLoco = this.locateDatObjectEdge(32, -55, -55);

				try {
					this.updatePath(objectFallingEdgeLoco, objectRisingEdgeLoco);
				} catch(NullPointerException e) {}

			} catch (FalseObjectException e) {}
		}

		if (odd) {
			uM.rotateSensorToWait(this.scanBand);
		} else {
			uM.rotateSensorToWait(-this.scanBand);
		}

		this.odd = !odd;
		return objectDetected;
	}

	private int correctedEdgeAngle(int i, boolean falling) {
		if (odd) {
			if (falling)
				return (i + fallingEdgeABSAngleCorrectionFactor);
			else
				return (i - risingEdgeABSAngleCorrectionFactor);
		}
		else {
			if (falling)
				return (i - fallingEdgeABSAngleCorrectionFactor);
			else
				return (i + risingEdgeABSAngleCorrectionFactor);
		}
	}


	private double wrapDatAngle(double angle) {
		if (angle < 0)
			angle += 360;
		else if (angle > 360)
			angle -= 360;
		return angle;
	}


	private void updatePath(double[] objectFallingEdgeLoco, double[] objectRisingEdgeLoco) throws FalseObjectException {
		int[] objectFallingEdgeNode = {(int) (objectFallingEdgeLoco[0] / 30.48), (int) (objectFallingEdgeLoco[1] / 30.48)};
		int[] objectRisingEdgeNode = {(int) (objectRisingEdgeLoco[0] / 30.48), (int) (objectRisingEdgeLoco[1] / 30.48)};
		int[] currentNode = {(int) (this.odo.getX() / 30.48), (int) (this.odo.getY() / 30.48)};

		// If it is a multi-square obstruction ...
		if (objectFallingEdgeNode[0] != objectRisingEdgeNode[0] || objectFallingEdgeNode[1] != objectRisingEdgeNode[1]) {
			this.dStarLite.updateCell(objectFallingEdgeNode[0], objectFallingEdgeNode[1], -1);
			this.dStarLite.updateCell(objectRisingEdgeNode[0], objectRisingEdgeNode[1], -1);

			//System.out.println("1:" + Arrays.toString(objectFallingEdgeNode));
			//System.out.println("2:" + Arrays.toString(objectRisingEdgeNode));

			if (objectFallingEdgeNode[0] != objectRisingEdgeNode[0] && objectFallingEdgeNode[1] != objectRisingEdgeNode[1]) {
				int[] diagonalNode = this.isDiagonalNode();
				if (diagonalNode != null) {
					this.dStarLite.updateCell(diagonalNode[0], diagonalNode[1], -1);
					//System.out.println("D:" + Arrays.toString(diagonalNode));
				}
			}

			this.dStarLite.replan();
		}
		else {
			double[] objectCenterLoco = {(objectFallingEdgeLoco[0] + objectRisingEdgeLoco[0]) / 2, (objectFallingEdgeLoco[1] + objectRisingEdgeLoco[1]) / 2};
			int[] objectCenterNode = {(int) (objectCenterLoco[0] / 30.48), (int) (objectCenterLoco[1] / 30.48)};
			this.dStarLite.updateCell(objectCenterNode[0], objectCenterNode[1], -1);
			this.dStarLite.replan();
		}

		Sound.beep();
	}

	private int[] isDiagonalNode() {
		double theta = this.odo.getAng();
		int[] node = {(int) (this.odo.getX() / 30.48), (int) (this.odo.getY() / 30.48)};

		if (theta > 22.5 && theta < 67.5) {
			node[0] += 1;
			node[1] += 1;
		}
		else if (theta > 112.5 && theta < 157.5) {
			node[0] -= 1;
			node[1] += 1;
		}
		else if (theta > 202.5 && theta < 247.5) {
			node[0] -= 1;
			node[1] -= 1;
		}
		else if (theta > 292.5 && theta < 337.5) {
			node[0] += 1;
			node[1] -= 1;
		}
		else
			node = null;

		return node;
	}

	// Locates the x and y of a given object
	private double[] locateDatObjectEdge(double distance, int correctedAngle, int i) throws FalseObjectException {
		double theta = odo.getAng();
		// Use the actual US sensor angle to calculate its position
		double[] sensorLoco = this.locateDatSensorLoco(theta, i);

		// Use the corrected angle when determining the object edge location.
		double ABSAngle = this.wrapDatAngle(correctedAngle + theta);

		double objectEdgeX = (sensorLoco[0] + distance * Math.cos(Math.toRadians(ABSAngle)));
		double objectEdgeY = (sensorLoco[1] + distance * Math.sin(Math.toRadians(ABSAngle)));

		//if (objectEdgeX <= 4|| objectEdgeY <= 4 || objectEdgeX >= ((30.48 * this.mapSize) - 4)|| objectEdgeY >= ((30.48 * this.mapSize) - 4))
		//throw new FalseObjectException();

		double[] objectLoco = {objectEdgeX, objectEdgeY};
		return objectLoco;
	}

	// Locates the location of the Ultrasonic sensor
	private double[] locateDatSensorLoco(double theta, int i) {
		// Locate medium motor location.
		double sensorX = odo.getX() + this.sensorAxleOffset * Math.cos(Math.toRadians(theta));
		double sensorY = odo.getY() + this.sensorAxleOffset * Math.sin(Math.toRadians(theta));

		// Locate location (the front) of Ultrasonic sensor
		sensorX += this.sensorRotationOffset * Math.cos(Math.toRadians(this.wrapDatAngle(i + theta)));
		sensorY += this.sensorRotationOffset * Math.sin(Math.toRadians(this.wrapDatAngle(i + theta)));

		double[] sensorLoco = {sensorX, sensorY};
		return sensorLoco;
	}

	private double cycleUSsensor(int cycleCount) {

		long startTime;
		// cycle USsensor for cycleCount - 1
		for (int i = 1; i < cycleCount; i++) {
			startTime = System.currentTimeMillis();
			uM.getDistance();
			if (System.currentTimeMillis() - startTime < 25)
				this.sleepFor(25);
		}

		return uM.getDistance();

	}

	private double rotateAndScan(int i) {

		uM.rotateSensorTo(i);
		this.sleepFor(50);

		for (int j = 0; j < 2; j++) {
			uM.getDistance();
			this.sleepFor(25);
		}

		double distance = uM.getDistance();
		this.sleepFor(25);

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
		synchronized(this) {
			return active;
		}
	}

	public void setActive(boolean active) {
		synchronized(this) {
			this.active = active;
		}
	}

	public boolean isObjectDetected() {
		synchronized(this) {
			return objectDetected;
		}
	}

	public void setObjectDetected(boolean objectDetected) {
		synchronized(this) {
			this.objectDetected = objectDetected;
		}
	}


}