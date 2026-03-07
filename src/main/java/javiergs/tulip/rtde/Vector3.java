package javiergs.tulip.rtde;

public class Vector3 {
	
	public double x, y, z;
	
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3 add(Vector3 v) {
		return new Vector3(x + v.x, y + v.y, z + v.z);
	}
	
	public Vector3 subtract(Vector3 v) {
		return new Vector3(x - v.x, y - v.y, z - v.z);
	}
	
	public Vector3 scale(double s) {
		return new Vector3(x * s, y * s, z * s);
	}
	
	public double dot(Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	public Vector3 cross(Vector3 v) {
		return new Vector3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
	}
	
	public Vector3 rotate(Vector3 axis, double angle) {
		double cosA = Math.cos(angle);
		double sinA = Math.sin(angle);
		return this.scale(cosA).add(axis.cross(this).scale(sinA)).add(axis.scale(axis.dot(this) * (1 - cosA)));
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Vector3)) return false;
		Vector3 v = (Vector3) o;
		return Double.compare(x, v.x) == 0 &&
			Double.compare(y, v.y) == 0 &&
			Double.compare(z, v.z) == 0;
	}
	
	@Override
	public String toString() {
		return "Vector3(" + x + ", " + y + ", " + z + ")";
	}
	
}