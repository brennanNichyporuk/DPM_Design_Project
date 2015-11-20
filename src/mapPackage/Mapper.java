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
public class Mapper{
	private Odometer odo;
	private UltrasonicModule uM;
	private DStarLite dStarLite;

	private boolean odd;
	private int scanBand = 90;
	private int scanIncrement = 45;
	private int sensorAxleOffset = 10;


	/**
	 * Instantiates a map.
	 * @param odo - reference to the Odometer thread.
	 * @param uM - reference to an instance of the UltrasonicModule
	 */
	public Mapper(Odometer odo, UltrasonicModule uM, DStarLite dStarLite) {
		this.odo = odo;
		this.uM = uM;
		this.dStarLite = dStarLite;
		this.odd = true;
	}

	/**
	 * This method uses the ULtrasonic sensor to scan its environment and update 
	 * the favorability of each node based on the presence or absence of obstacles.
	 * @param x - present node x
	 * @param y - present node y
	 * @return - updated map.
	 */

	public void scan() {
		double distance;
		double currentAngle = this.odo.getAng();
		double ABSAngle;
		if (odd) {
			for (int i = -scanBand; i <= scanBand; i += scanIncrement) {
				this.uM.rotateSensorTo(i);
				ABSAngle = this.wrapDatAngle(currentAngle + i);
				distance = this.cycleUSsensor(5);
				if (i == 0) {
					if (distance < 40) {
						this.disQualifyNode(ABSAngle);
					}
					
				}
				else if (i == 45 || i == -45) {
					if (distance < 35) {
						this.disQualifyNode(ABSAngle);
					}
					
				}
				else {
					if (distance < 45) {
						this.disQualifyNode(ABSAngle);
					}
					
				}
				
			}
			
		}
		else {
			for (int i = scanBand; i >= -scanBand; i -= scanIncrement) {
				this.uM.rotateSensorTo(i);
				ABSAngle = this.wrapDatAngle(currentAngle + i);
				distance = this.cycleUSsensor(5);
				if (i == 0) {
					if (distance < 40) {
						this.disQualifyNode(ABSAngle);
					}
				}
				else if (i == 45 || i == -45) {
					if (distance < 35) {
						this.disQualifyNode(ABSAngle);
					}
				}
				else {
					if (distance < 45) {
						this.disQualifyNode(ABSAngle);
					}
				}
			}
		}
	}

	private void disQualifyNode(double ABSAngle) {
		int currentLocoNodeX = (int) (this.odo.getX() / 30.48);
		int currentLocoNodeY = (int) (this.odo.getY() / 30.48);
		int deltaX = 0;
		int deltaY = 0;

		if (ABSAngle > 337.5 || ABSAngle < 22.5) {
			deltaX = 1;
			deltaY = 0;
		}
		else if (ABSAngle > 22.5 && ABSAngle < 67.5) {
			deltaX = 1;
			deltaY = 1;
		}
		else if (ABSAngle > 67.5 && ABSAngle < 112.5) {
			deltaX = 0;
			deltaY = 1;
		}
		else if (ABSAngle > 112.5 && ABSAngle < 157.5) {
			deltaX = -1;
			deltaY = 1;
		}
		else if (ABSAngle > 157.5 && ABSAngle < 202.5) {
			deltaX = -1;
			deltaY = 0;
		}
		else if (ABSAngle > 202.5 && ABSAngle < 247.5) {
			deltaX = -1;
			deltaY = -1;
		}
		else if (ABSAngle > 247.5 && ABSAngle < 292.5) {
			deltaX = 0;
			deltaY = -1;
		}
		else if (ABSAngle > 292.5 && ABSAngle < 337.5) {
			deltaX = 1;
			deltaY = -1;
		}

		int dNodeX = currentLocoNodeX + deltaX;
		int dNodeY = currentLocoNodeY + deltaY;

		if (dNodeX >= 0 && dNodeX <= 7 && dNodeY >= 0 && dNodeY <= 7) {
			Sound.beep();
			this.dStarLite.updateCell(dNodeX, dNodeY, -1);
			this.dStarLite.replan();
		}
	}

	private double cycleUSsensor(int cycleCount) {

		long t;
		double distance = 0;
		for (int i = 0; i < cycleCount; i++) {
			t = System.currentTimeMillis();
			distance = uM.getDistance();
			t = System.currentTimeMillis() - t;
			if (t < 25)
				this.sleepFor(t);
		}

		return distance;

	}

	private double wrapDatAngle(double angle) {
		if (angle < 0)
			angle += 360;
		else if (angle > 360)
			angle -= 360;
		return angle;
	}

	private void sleepFor(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}