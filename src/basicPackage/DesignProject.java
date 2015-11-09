package basicPackage;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;

public class DesignProject {
	public static void main(String[] args) {
		// Test Comment
		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
		Odometer odo = new Odometer(leftMotor, rightMotor, 20, true);
		Navigation navigator = new Navigation(odo);
		LCDInfo lcd = new LCDInfo(odo);
		navigator.turnTo(0, true);
		navigator.travelTo(60, 0);
		sleep(3000);
		navigator.travelTo(60, 60);
		sleep(3000);
		navigator.travelTo(0,60);
		sleep(3000);
		navigator.travelTo(0, 0);
		navigator.turnTo(0, true);
		
	}
	public static void sleep(int sleepTime){
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
