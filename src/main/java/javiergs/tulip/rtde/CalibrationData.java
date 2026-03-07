package javiergs.tulip.rtde;

/**
 * Extract from a byte array the calibration data of the robot (48 bytes or 6 doubles)
 *
 * @author ur
 * @version 1.0, 2024-08-10
 */
public class CalibrationData {
	
	private double Fx;
	private double Fy;
	private double Fz;
	private double Frx;
	private double Fry;
	private double Frz;
	
	
	public CalibrationData setFx(double v) {
		Fx = v;
		return this;
	}
	
	public double getFx() {
		return Fx;
	}
	
	public CalibrationData setFy(double v) {
		Fy = v;
		return this;
	}
	
	public double getFy() {
		return Fy;
	}
	
	public CalibrationData setFz(double v) {
		Fz = v;
		return this;
	}
	
	public double getFz() {
		return Fz;
	}
	
	public CalibrationData setFrx(double v) {
		Frx = v;
		return this;
	}
	
	public double getFrx() {
		return Frx;
	}
	
	public CalibrationData setFry(double v) {
		Fry = v;
		return this;
	}
	
	public double getFry() {
		return Fry;
	}
	
	public CalibrationData setFrz(double v) {
		Frz = v;
		return this;
	}
	
	public double getFrz() {
		return Frz;
	}
	
}