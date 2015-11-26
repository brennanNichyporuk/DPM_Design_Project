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
	private double sensorAxleOffset = 7.5;

	private final int maxRange = 50;
	private final int minDerivativeChange = 6;
	private int scanBand = 90;
	private final int scanIncrement = 5;
	private final int mapSize = 8;
	private boolean active;
	private boolean objectDetected;
	private boolean odd;
	
	private int risingEdgeABSAngleCorrectionFactor = 22, fallingEdgeABSAngleCorrectionFactor = 8;
	
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

		/*
		for (int i = 0; i < this.mapSize; i++)
			dStarLite.updateCell(i, this.mapSize, -1);

		dStarLite.updateCell(this.mapSize, this.mapSize, -1);

		for (int j = 0; j < this.mapSize; j++)
			dStarLite.updateCell(this.mapSize, j, -1);

		dStarLite.replan();
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
				this.objectDetected = this.scan();
				this.active = true;
			}
			else
				this.sleepFor(100);
		}
	}

	public boolean scan() {
		double distance = 0, lastDistance = 0, fallingEdgeDistance = 0;
		double[] objectFallingEdgeLoco = null, objectRisingEdgeLoco = null, objectLoco = null;
		int lastABSAngle = 0, fallingEdgeABSAngle = 0;
		boolean objectDetected = false, fallingEdgeDetected = false, fallingEdgeRegistered = false;
		
		lastDistance = this.cycleUSsensor(5);
		lastABSAngle = 0;
		int i;
		if (odd)
			i = -scanBand;
		else
			i = scanBand;

		while (Math.abs(i) <= Math.abs(scanBand)) {
			//System.out.println(i);
			distance = this.rotateAndScan(i);
			double derivative = distance - lastDistance;
			int ABSAngle = (int) this.wrapDatAngle(i + odo.getAng());
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
						//System.out.println("rED:" + (int) lastDistance);
						//this.sleepFor(10000);

						try {
							double correctedFallingEdgeABSAngle, correctedRisingEdgeABSAngle;
							if (odd) {
								correctedFallingEdgeABSAngle = fallingEdgeABSAngle + fallingEdgeABSAngleCorrectionFactor;
								correctedRisingEdgeABSAngle = lastABSAngle - risingEdgeABSAngleCorrectionFactor;
							}
							else {
								correctedFallingEdgeABSAngle = fallingEdgeABSAngle - fallingEdgeABSAngleCorrectionFactor;
								correctedRisingEdgeABSAngle = lastABSAngle + risingEdgeABSAngleCorrectionFactor;
							}
							
							// CORRECT RISING EDGE DISCREPENCY AS DIRECTED TO BY EDGE DETECTION TESTING
							lastDistance -= 2.75;
			
							//TEMP CODE:
							correctedFallingEdgeABSAngle -= 90;
							correctedRisingEdgeABSAngle -= 90;
							System.out.println("oFeD:" + (int) fallingEdgeDistance);
							this.sleepFor(5000);
							//objectFallingEdgeLoco = this.locateDatObjectEdge(fallingEdgeDistance, correctedFallingEdgeABSAngle);
							//int[] oFELoco = {(int) objectFallingEdgeLoco[0], (int) objectFallingEdgeLoco[1]};
							System.out.println("oFeA:" + (int) correctedFallingEdgeABSAngle);
							this.sleepFor(5000);
							System.out.println("oReD:" + (int) lastDistance);
							this.sleepFor(5000);
							//objectRisingEdgeLoco = this.locateDatObjectEdge(lastDistance, correctedRisingEdgeABSAngle);
							if (false)
								throw new FalseObjectException();
							//int[] oRELoco = {(int) objectRisingEdgeLoco[0], (int) objectRisingEdgeLoco[1]};
							System.out.println("oReA:" + (int) correctedRisingEdgeABSAngle);
							this.sleepFor(5000);
							// END OF TEMP CODE
							// Update the path ...
							//this.updatePath(objectFallingEdgeLoco, objectRisingEdgeLoco);
							objectDetected = true;
						} catch (FalseObjectException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						}
						fallingEdgeRegistered = false;
					}
				}
			} 
			else {
				// This records the first distance and angle that does not meet the minimum derivative
				// provided the a falling edge was previously detected.
				if (fallingEdgeDetected) {
					fallingEdgeDistance = distance;
					fallingEdgeABSAngle = ABSAngle;
					//System.out.println("fED:" + (int) fallingEdgeDistance);
					fallingEdgeDetected = false;
					fallingEdgeRegistered = true;
				}
			}
			lastABSAngle = ABSAngle;
			lastDistance = distance;

			if (odd)
				i += scanIncrement;
			else
				i -= scanIncrement;
		}

		if (odd) {
			uM.rotateSensorToWait(this.scanBand);
		} else {
			uM.rotateSensorToWait(-this.scanBand);
		}
		
		this.odd = !odd;
		return objectDetected;
	}



	private double wrapDatAngle(double angle) {
		if (angle < 0)
			angle += 360;
		else if (angle > 360)
			angle -= 360;
		return angle;
	}


	private void updatePath(double[] objectFallingEdgeLoco, double[] objectRisingEdgeLoco) throws FalseObjectException {
		double[] objectCenterLoco = {(objectFallingEdgeLoco[0] + objectRisingEdgeLoco[0]) / 2, (objectFallingEdgeLoco[1] + objectRisingEdgeLoco[1]) / 2};
		//int[] objectCenterNode = {(int) (objectCenterLoco[0] / 30.48), (int) (objectCenterLoco[1] / 30.48)};

		// TEMP CODE:
		int[] objectCenterLocoInt = {(int) objectCenterLoco[0], (int) objectCenterLoco[1]};
		//System.out.println("C:" + Arrays.toString(objectCenterLocoInt));
		//this.dStarLite.updateCell(objectCenterNode[0], objectCenterNode[1], -1);
		//this.dStarLite.replan();
		Sound.beep();
		this.sleepFor(5000);
	}

	// Locates the x and y of a given object
	private double[] locateDatObjectEdge(double distance, double angle) throws FalseObjectException {
		double[] sensorLoco = this.locateDatSensorLoco();
		double objectEdgeX = (sensorLoco[0] + distance * Math.cos(Math.toRadians(angle)));
		double objectEdgeY = (sensorLoco[1] + distance * Math.sin(Math.toRadians(angle)));
		//if (objectEdgeX <= 5|| objectEdgeY <= 5 || objectEdgeX >= ((30.48 * this.mapSize) - 5)|| objectEdgeY >= ((30.48 * this.mapSize) - 5))
			//throw new FalseObjectException();
		double[] objectLoco = {objectEdgeX, objectEdgeY};
		return objectLoco;
	}

	// Locates the location of the Ultrasonic sensor
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