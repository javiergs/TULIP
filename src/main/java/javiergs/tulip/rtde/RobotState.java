package javiergs.tulip.rtde;

/**
 * A container for the robot state data, including calibration, joints, Cartesian, kinematics, and mode data.
 * Each sub-data class holds specific information about the robot's current state.
 *
 * @author javiergs
 * @version 1.0, 2025-07-11
 */
public class RobotState {
	
	private CalibrationData calibrationData;
	private JointsData jointsData;
	private CartesianData cartesianData;
	private KinematicsData kinematicsData;
	private ModeData modeData;

	public RobotState() {
		calibrationData = new CalibrationData();
		jointsData = new JointsData();
		cartesianData = new CartesianData();
		kinematicsData = new KinematicsData();
		modeData = new ModeData();
	}
	
	public CalibrationData getCalibrationData() {
		return calibrationData;
	}
	
	public void setCalibrationData(CalibrationData calibrationData) {
		this.calibrationData = calibrationData;
	}
	
	public JointsData getJointsData() {
		return jointsData;
	}
	
	public void setJointsData(JointsData jointsData) {
		this.jointsData = jointsData;
	}
	
	public CartesianData getCartesianData() {
		return cartesianData;
	}
	
	public void setCartesianData(CartesianData cartesianData) {
		this.cartesianData = cartesianData;
	}
	
	public KinematicsData getKinematicsData() {
		return kinematicsData;
	}
	
	public void setKinematicsData(KinematicsData kinematicsData) {
		this.kinematicsData = kinematicsData;
	}
	
	public ModeData getModeData() {
		return modeData;
	}
	
	public void setModeData(ModeData modeData) {
		this.modeData = modeData;
	}
	
}
