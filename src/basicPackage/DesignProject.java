package basicPackage;

import java.io.IOException;
import java.util.List;

import executivePackage.Planner;
import pilotPackage.DStarLite;
import pilotPackage.Pilot;
import pilotPackage.State;
import wifi.StartCorner;
import wifi.Transmission;
import wifi.WifiConnection;
import basicPackage.USLocalizer.LocalizationType;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import mapPackage.Mapper;
import modulePackage.LineDetection;
import modulePackage.UltrasonicModule;

public class DesignProject {
	/**
	 * ip address as detailed in specifications of first demo.
	 */
	private static final String SERVER_IP = "192.168.10.200";
	private static final int TEAM_NUMBER = 10;
	private static TextLCD LCD = LocalEV3.get().getTextLCD();

	public static void main(String args[]){
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
					int opponentHomeZoneTR_X= t.opponentHomeZoneTR_X;
					int opponentHomeZoneTR_Y= t.opponentHomeZoneTR_Y;
					int dropZone_X = t.dropZone_X;
					int dropZone_Y = t.dropZone_Y;
					int flagType = t.flagType;
					int	opponentFlagType = t.opponentFlagType;
					//starting corner, opponentHomeZoneLowX, opponentHomeZoneLowY, opponentHomeZoneHighX, opponentHomeZoneHighY, dropZoneX, dropZoneY, flagtype
					Planner planner = new Planner(corner.getId(),opponentHomeZoneBL_X,opponentHomeZoneBL_Y,opponentHomeZoneTR_X,opponentHomeZoneTR_Y,dropZone_X,dropZone_Y,flagType);
					conn.printTransmission();
				}
				
				Button.waitForAnyPress();
				System.exit(0);
		 	}
}
