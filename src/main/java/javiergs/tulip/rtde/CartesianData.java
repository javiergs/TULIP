package javiergs.tulip.rtde;

public class CartesianData {
	
	private double x;
	private double y;
	private double z;
	private double rx;
	private double ry;
	private double rz;
	private double TCPOffsetX;
	private double TCPOffsetY;
	private double TCPOffsetZ;
	private double TCPOffsetRX;
	private double TCPOffsetRY;
	private double TCPOffsetRZ;
	
	public double getX() {
		return x;
	}
	
	public CartesianData setX(double x) {
		this.x = x;
		return this;
	}
	
	public double getY() {
		return y;
	}
	
	public CartesianData setY(double y) {
		this.y = y;
		return this;
	}
	
	public double getZ() {
		return z;
	}
	
	public CartesianData setZ(double z) {
		this.z = z;
		return this;
	}
	
	public double getRx() {
		return rx;
	}
	
	public CartesianData setRx(double rx) {
		this.rx = rx;
		return this;
	}
	
	public double getRy() {
		return ry;
	}
	
	public CartesianData setRy(double ry) {
		this.ry = ry;
		return this;
	}
	
	public double getRz() {
		return rz;
	}
	
	public CartesianData setRz(double rz) {
		this.rz = rz;
		return this;
	}
	
	public double getTCPOffsetX() {
		return TCPOffsetX;
	}
	
	public CartesianData setTCPOffsetX(double TCPOffsetX) {
		this.TCPOffsetX = TCPOffsetX;
		return this;
	}
	
	public double getTCPOffsetY() {
		return TCPOffsetY;
	}
	
	public CartesianData setTCPOffsetY(double TCPOffsetY) {
		this.TCPOffsetY = TCPOffsetY;
		return this;
	}
	
	public double getTCPOffsetZ() {
		return TCPOffsetZ;
	}
	
	public CartesianData setTCPOffsetZ(double TCPOffsetZ) {
		this.TCPOffsetZ = TCPOffsetZ;
		return this;
	}
	
	public double getTCPOffsetRX() {
		return TCPOffsetRX;
	}
	
	public CartesianData setTCPOffsetRX(double TCPOffsetRX) {
		this.TCPOffsetRX = TCPOffsetRX;
		return this;
	}
	
	public double getTCPOffsetRY() {
		return TCPOffsetRY;
	}
	
	public CartesianData setTCPOffsetRY(double TCPOffsetRY) {
		this.TCPOffsetRY = TCPOffsetRY;
		return this;
	}
	
	public double getTCPOffsetRZ() {
		return TCPOffsetRZ;
	}
	
	public CartesianData setTCPOffsetRZ(double TCPOffsetRZ) {
		this.TCPOffsetRZ = TCPOffsetRZ;
		return this;
	}
	
	public String toString() {
		return
			String.format("%.3f", x) + ", " +
			String.format("%.3f", y) + ", " +
			String.format("%.3f", z) + ", " +
			String.format("%.3f", rx) + ", " +
			String.format("%.3f", ry) + ", " +
			String.format("%.3f", rz);
	}
	
	public double[] getPosition() {
		return new double[]{x, y, z};
	}
	
	public double[] getOrientation() {
		return new double[]{rx, ry, rz};
	}
	
	public double[] get() {
		return new double[]{x, y, z, rx, ry, rz};
	}
}
