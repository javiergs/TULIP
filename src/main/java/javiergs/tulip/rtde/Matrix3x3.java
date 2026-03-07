package javiergs.tulip.rtde;

public class Matrix3x3 {
	
	public double[] m = new double[9];
	
	public Matrix3x3(double[] values) {
		if (values.length == 9) {
			System.arraycopy(values, 0, m, 0, 9);
		} else {
			throw new IllegalArgumentException("Matrix3x3 requires 9 values");
		}
	}
	
	public Matrix3x3 multiply(Matrix3x3 other) {
		double[] result = new double[9];
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				result[row * 3 + col] = m[row * 3] * other.m[col] + m[row * 3 + 1] * other.m[col + 3] + m[row * 3 + 2] * other.m[col + 6];
			}
		}
		return new Matrix3x3(result);
	}
	
	public Vector3 apply(Vector3 v) {
		double x = m[0] * v.x + m[1] * v.y + m[2] * v.z;
		double y = m[3] * v.x + m[4] * v.y + m[5] * v.z;
		double z = m[6] * v.x + m[7] * v.y + m[8] * v.z;
		return new Vector3(x, y, z);
	}
	
	public static Matrix3x3 rotateX(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		return new Matrix3x3(new double[]{1, 0, 0, 0, c, -s, 0, s, c});
	}
	
	public static Matrix3x3 rotateY(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		return new Matrix3x3(new double[]{c, 0, s, 0, 1, 0, -s, 0, c});
	}
	
	public static Matrix3x3 rotateZ(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		return new Matrix3x3(new double[]{c, -s, 0, s, c, 0, 0, 0, 1});
	}
	
	public static Matrix3x3 identity() {
		return new Matrix3x3(new double[]{1, 0, 0, 0, 1, 0, 0, 0, 1});
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Matrix3x3)) return false;
		Matrix3x3 other = (Matrix3x3) o;
		for (int i = 0; i < 9; i++) {
			if (Double.compare(m[i], other.m[i]) != 0) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("Matrix3x3([\n  %.3f, %.3f, %.3f,\n  %.3f, %.3f, %.3f,\n  %.3f, %.3f, %.3f\n])",
			m[0], m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8]);
	}
	
	public double get(int i, int i1) {
		return m[i*3 + i1];
	}
}
