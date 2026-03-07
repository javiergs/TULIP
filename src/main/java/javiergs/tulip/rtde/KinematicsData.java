package javiergs.tulip.rtde;

public class KinematicsData {
	
	private int[] checksum;
	private double[] DHtheta;
	private double[] DHa;
	private double[] DHd;
	private double[] DHalpha;
	private int calib;
	
	public KinematicsData() {
		checksum = new int[6];
		DHtheta = new double[6];
		DHa = new double[6];
		DHd = new double[6];
		DHalpha = new double[6];
		calib = 0;
	}
	
	public int getChecksum(int index) {
		return checksum[index];
	}
	
	public double getDHtheta(int index) {
		return DHtheta[index];
	}
	
	public double getDHa(int index) {
		return DHa[index];
	}
	
	public double getDHd(int index) {
		return DHd[index];
	}
	
	public double getDHalpha(int index) {
		return DHalpha[index];
	}
	
	public int getCalib() {
		return calib;
	}
	
	public void setChecksum(int i, int value) {
		checksum[i] = value;
	}
	
	public void setDHtheta(int i, double value) {
		DHtheta[i] = value;
	}
	
	public void setDHa(int i, double value) {
		DHa[i] = value;
	}
	
	public void setDHd(int i, double value) {
		DHd[i] = value;
	}
	
	public void setDHalpha(int i, double value) {
		DHalpha[i] = value;
	}
	
	public void setCalib(int value) {
		calib = value;
	}
	
}
