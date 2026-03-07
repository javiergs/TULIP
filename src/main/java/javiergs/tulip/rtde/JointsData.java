package javiergs.tulip.rtde;

/**
 * A container for the joint data of the robot,
 * including actual and target joint positions, velocities, currents, voltages, motor torques, micro torques, and modes.
 * Each array holds data for 6 joints (0 to 5).
 *
 * @author javiergs
 * @version 2025-07-11
 */
public class JointsData {
	
	private double[] qActual;
	private double[] qTarget; // this
	private double[] qdActual;
	private float[] IActual;
	private float[] VActual;
	private float[] TMotor;
	private float[] TMicro;
	private int[] mode;
	
	public JointsData() {
		qActual = new double[6];
		qTarget = new double[6];
		qdActual = new double[6];
		IActual = new float[6];
		VActual = new float[6];
		TMotor = new float[6];
		TMicro = new float[6];
		mode = new int[6];
	}
	
	public double getQActual(int index) {
		return qActual[index];
	}
	
	public void setQActual(int index, double value) {
		qActual[index] = value;
	}
	
	public double getQTarget(int index) {
		return qTarget[index];
	}
	
	public void setQTarget(int index, double value) {
		qTarget[index] = value;
	}
	
	public double getQdActual(int index) {
		return qdActual[index];
	}
	
	public void setQdActual(int index, double value) {
		qdActual[index] = value;
	}
	
	public float getIActual(int index) {
		return IActual[index];
	}
	
	public void setIActual(int index, float value) {
		IActual[index] = value;
	}
	
	public float getVActual(int index) {
		return VActual[index];
	}
	
	public void setVActual(int index, float value) {
		VActual[index] = value;
	}
	
	public float getTMotor(int index) {
		return TMotor[index];
	}
	
	public void setTMotor(int index, float value) {
		TMotor[index] = value;
	}
	
	public float getTMicro(int index) {
		return TMicro[index];
	}
	
	public void setTMicro(int index, float value) {
		TMicro[index] = value;
	}
	
	public int getMode(int index) {
		return mode[index];
	}
	
	public void setMode(int index, int value) {
		mode[index] = value;
	}
	
	public double[] get () {
		return qTarget;
	}
	
	public String toString() {
		String str = "";
		for (int i = 0; i < qTarget.length; i++) {
			str += String.format("%.3f", qTarget[i]) + ", ";
		}
		return str;
	}
	
	public void set(double[] actualQ) {
		if (actualQ != null && actualQ.length == 6) {
			System.arraycopy(actualQ, 0, this.qActual, 0, 6);
			System.arraycopy(actualQ, 0, this.qTarget, 0, 6); // for simplicity, set target same as actual
		}
	}
}
