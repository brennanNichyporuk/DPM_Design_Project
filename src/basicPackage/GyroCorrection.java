package basicPackage;

import lejos.hardware.sensor.EV3GyroSensor;
import lejos.utility.Delay;

public class GyroCorrection implements Runnable{
	EV3GyroSensor gyro;
	float[] angleSamples;
	double offset;
	Odometer odo;
	double initialAngle;
	public GyroCorrection(EV3GyroSensor gyro){
		this.gyro = gyro;
		this.initialAngle= 0.0;
		gyro.reset();
		this.angleSamples = new float[2];
	}
	public double correctOrientation(){
		double sum=0;
		for(int i = 0; i < 100; i = i + 1){
	         gyro.getAngleMode().fetchSample(angleSamples, 0);
	         sum+=angleSamples[0];
	      }
		double orientation = (sum/100);
		return this.fixGyroAngle(-orientation);
	}
	public double fixGyroAngle(double angle){
		angle %=360;
		if(angle<0){
			angle +=360;
		}
		return angle+this.initialAngle;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			boolean update[] = {false,false,true};
			double position[] = {0,0,this.correctOrientation()};
			this.odo.setPosition(position, update);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
