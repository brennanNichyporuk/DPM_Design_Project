package basicPackage;

import lejos.hardware.sensor.EV3GyroSensor;
import lejos.utility.Delay;

public class GyroCorrection{
	
	/**
	 * Instance of the gyrosensor for angle correction.
	 */
	EV3GyroSensor gyro;
	/**
	 * array that holds values from the gyrosensor
	 */
	float[] angleSamples;
	/**
	 * starting orientation of the robot. 
	 */
	double initialAngle;
	public GyroCorrection(EV3GyroSensor gyro){
		this.gyro = gyro;
		this.initialAngle= 90.0;
		gyro.reset();
		this.angleSamples = new float[2];
	}
	
	/**
	 * 
	 * @return returns the orientation of the robot as determined by the gyroscope.
	 */
	public double correctOrientation(){
		double sum=0;
		for(int i = 0; i < 500; i = i + 1){
	         gyro.getAngleMode().fetchSample(angleSamples, 0);
	         sum+=angleSamples[0];
	      }
		double orientation = (sum/500);
		return this.fixGyroAngle(-orientation);
	}
	/**
	 * 
	 * @param helper method to calculate the correct orientation of the gyroscope based on it's current orientation
	 * @return returns the fixed angle based on the robot's configuration.
	 */
	private double fixGyroAngle(double angle){
		angle %=360;
		if(angle<0){
			angle +=360;
		}
		return angle+this.initialAngle;
	}
	/**
	 * resets the gyroscope to zero 
	 */
	public void resetGyro(){
		gyro.reset();
	}
	/**
	 * 
	 * @param angle the angle you want to set the intial gyroscope angle to.
	 */
	public void setGyro(double angle){
		this.initialAngle = angle;
		gyro.reset();
	}
}
