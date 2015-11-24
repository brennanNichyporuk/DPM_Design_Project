package wifiClientPackage;
/*
* @author Sean Lawlor
* @date November 3, 2011
* @class ECSE 211 - Design Principle and Methods
* 
* Modified by F.P. Ferrie
* February 28, 2014
* Changed parameters for W2014 competition
* 
* Modified by Francois OD
* November 11, 2015
* Ported to EV3 and wifi (from NXT and bluetooth)
* Changed parameters for F2015 competition
*/
import java.io.IOException;

import basicPackage.DesignProject;
import executivePackage.Planner;
import wifi.WifiConnection;
import wifi.StartCorner;
import wifi.Transmission;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;

public class WifiTest {
	// example call of the transmission protocol
	// The print function is just for debugging to make sure data is received correctly

	// *** INSTRUCTIONS ***
	// There are two variables to set manually on the EV3 client:
	// 1. SERVER_IP: the IP address of the computer running the server application
	// 2. TEAM_NUMBER: your project team number
	
	private static final String SERVER_IP = "192.168.10.200";
	private static final int TEAM_NUMBER = 10;
	
	private static TextLCD LCD = LocalEV3.get().getTextLCD();

	@SuppressWarnings("unused")
	public static void main(String [] args) {
		
		WifiConnection conn = null;
		try {
			conn = new WifiConnection(SERVER_IP, TEAM_NUMBER);
		} catch (IOException e) {
			LCD.drawString("Connection failed", 0, 8);
		}
		
		// example usage of Transmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			StartCorner corner = t.startingCorner;
			int homeZoneBL_X = t.homeZoneBL_X;
			int homeZoneBL_Y = t.homeZoneBL_Y;
			int opponentHomeZoneBL_X = t.opponentHomeZoneBL_X;
			int opponentHomeZoneBL_Y = t.opponentHomeZoneBL_Y;
			int dropZone_X = t.dropZone_X;
			int dropZone_Y = t.dropZone_Y;
			int flagType = t.flagType;
			int	opponentFlagType = t.opponentFlagType;
			
//			String[] inputs = {};
//			
//			DesignProject.main(inputs);
			
			int[] our_home_zone = {homeZoneBL_X, homeZoneBL_Y};
			int[] opponent_home_zone = {opponentHomeZoneBL_X, opponentHomeZoneBL_Y};
			int[] drop_zone = {dropZone_X, dropZone_Y};
			
			/**
			 * @param corner: starting corner of the robot.
			 * @param our_home_zone: int array with X and Y coordinates for our homezone. [0] -> X [1] -> Y
			 * @param opponent_home_zone: int array with X and Y coordinates for opponents homezone. [0] -> X [1] -> Y
			 * @param drop_zone: int array with X and Y coordinates for where we need to bring the flag. [0] -> X [1] -> Y
			 * @param flagType: Color of flag determined by an integer between 1-5. (Each block is associated a number before test)
			 */
			//Planner plan = new Planner(corner, our_home_zone, opponent_home_zone, drop_zone, flagType);
			// print out the transmission information
			conn.printTransmission();
			
			
			
		}
		// stall until user decides to end program
		Button.ESCAPE.waitForPress();
	}
}
