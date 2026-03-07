package javiergs.tulip.rtde;

public class ModeData {
	
	private long timestamp ;
	private boolean isRealRobotConnected ;
	private boolean isRealRobotEnabled;
	private boolean isRealRobotPowerOn ;
	private boolean isEmergencyStopped ;
	private boolean isProtectiveStopped ;
	private boolean isProgramRunning ;
	private boolean isProgramPaused ;
	private byte robotMode;
	private byte controlMode ;
	private double targetSpeedFraction ;
	private double speedScaling ;
	private double targetSpeedFractionLimit ;
	private byte reserved ;
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public ModeData setTimestamp(long timestamp) {
		this.timestamp = timestamp;
		return this;
	}
	
	public boolean isRealRobotConnected() {
		return isRealRobotConnected;
	}
	
	public ModeData setRealRobotConnected(boolean realRobotConnected) {
		isRealRobotConnected = realRobotConnected;
		return this;
	}
	
	public boolean isRealRobotEnabled() {
		return isRealRobotEnabled;
	}
	
	public ModeData setRealRobotEnabled(boolean realRobotEnabled) {
		isRealRobotEnabled = realRobotEnabled;
		return this;
	}
	
	public boolean isRealRobotPowerOn() {
		return isRealRobotPowerOn;
	}
	
	public ModeData setRealRobotPowerOn(boolean realRobotPowerOn) {
		isRealRobotPowerOn = realRobotPowerOn;
		return this;
	}
	
	public boolean isEmergencyStopped() {
		return isEmergencyStopped;
	}
	
	public ModeData setEmergencyStopped(boolean emergencyStopped) {
		isEmergencyStopped = emergencyStopped;
		return this;
	}
	
	public boolean isProtectiveStopped() {
		return isProtectiveStopped;
	}
	
	public ModeData setProtectiveStopped(boolean protectiveStopped) {
		isProtectiveStopped = protectiveStopped;
		return this;
	}
	
	public boolean isProgramRunning() {
		return isProgramRunning;
	}
	
	public ModeData setProgramRunning(boolean programRunning) {
		isProgramRunning = programRunning;
		return this;
	}
	
	public boolean isProgramPaused() {
		return isProgramPaused;
	}
	
	public ModeData setProgramPaused(boolean programPaused) {
		isProgramPaused = programPaused;
		return this;
	}
	
	public byte getRobotMode() {
		return robotMode;
	}
	
	public ModeData setRobotMode(byte robotMode) {
		this.robotMode = robotMode;
		return this;
	}
	
	public byte getControlMode() {
		return controlMode;
	}
	
	public ModeData setControlMode(byte controlMode) {
		this.controlMode = controlMode;
		return this;
	}
	
	public double getTargetSpeedFraction() {
		return targetSpeedFraction;
	}
	
	public ModeData setTargetSpeedFraction(double targetSpeedFraction) {
		this.targetSpeedFraction = targetSpeedFraction;
		return this;
	}
	
	public double getSpeedScaling() {
		return speedScaling;
	}
	
	public ModeData setSpeedScaling(double speedScaling) {
		this.speedScaling = speedScaling;
		return this;
	}
	
	public double getTargetSpeedFractionLimit() {
		return targetSpeedFractionLimit;
	}
	
	public ModeData setTargetSpeedFractionLimit(double targetSpeedFractionLimit) {
		this.targetSpeedFractionLimit = targetSpeedFractionLimit;
		return this;
	}
	
	public byte getReserved() {
		return reserved;
	}
	
	public ModeData setReserved(byte reserved) {
		this.reserved = reserved;
		return this;
	}
	
	public boolean getIsProtectiveStopped() {
		return isProtectiveStopped;
	}
	
	public boolean getIsEmergencyStopped() {
		return isEmergencyStopped;
	}
}

