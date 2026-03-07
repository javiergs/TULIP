package javiergs.tulip.rtde;

public class Matrix4 {
	private final double[][] m;
	
	public Matrix4(double[][] m) {
		this.m = m;
	}
	
	public static Matrix4 identity() {
		double[][] m = new double[4][4];
		for (int i = 0; i < 4; i++) m[i][i] = 1;
		return new Matrix4(m);
	}
	
	public static Matrix4 translation(double x, double y, double z) {
		Matrix4 t = identity();
		t.m[0][3] = x;
		t.m[1][3] = y;
		t.m[2][3] = z;
		return t;
	}
	
	public static Matrix4 rotationX(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		double[][] m = {
			{1, 0,  0, 0},
			{0, c, -s, 0},
			{0, s,  c, 0},
			{0, 0,  0, 1}
		};
		return new Matrix4(m);
	}
	
	public static Matrix4 rotationY(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		double[][] m = {
			{ c, 0, s, 0},
			{ 0, 1, 0, 0},
			{-s, 0, c, 0},
			{ 0, 0, 0, 1}
		};
		return new Matrix4(m);
	}
	
	public static Matrix4 rotationZ(double angle) {
		double c = Math.cos(angle), s = Math.sin(angle);
		double[][] m = {
			{c, -s, 0, 0},
			{s,  c, 0, 0},
			{0,  0, 1, 0},
			{0,  0, 0, 1}
		};
		return new Matrix4(m);
	}
	
	public Matrix4 multiply(Matrix4 other) {
		double[][] result = new double[4][4];
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 4; col++) {
				for (int k = 0; k < 4; k++) {
					result[row][col] += this.m[row][k] * other.m[k][col];
				}
			}
		}
		return new Matrix4(result);
	}
	
	public Vector3 transformPoint(Vector3 v) {
		double[] res = new double[4];
		double[] vec = {v.x, v.y, v.z, 1};
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				res[i] += m[i][j] * vec[j];
			}
		}
		return new Vector3(res[0], res[1], res[2]);
	}
	
	public static Matrix4 scale(double sx, double sy, double sz) {
		double[][] m = {
			{sx, 0,  0,  0},
			{0,  sy, 0,  0},
			{0,  0,  sz, 0},
			{0,  0,  0,  1}
		};
		return new Matrix4(m);
	}
	
	public double get(int i, int i1) {
		return m[i][i1];
	}
	
	public Matrix3x3 extractRotation() {
		return new Matrix3x3(new double[]{
			m[0][0], m[0][1], m[0][2],
			m[1][0], m[1][1], m[1][2],
			m[2][0], m[2][1], m[2][2]
		});
	}
}
