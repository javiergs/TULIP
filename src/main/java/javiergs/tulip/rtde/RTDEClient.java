package javiergs.tulip.rtde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * RTDEClient handles the RTDE protocol communication with a Universal Robots controller.
 *
 * @author javiergs
 * @version 1.0, 2025-07-11
 * @see <a href="https://sdurobotics.gitlab.io/ur_rtde/">UR RTDE GitLab</a>
 */
public class RTDEClient extends PropertyChangeSupport {
	
	private static final int RTDE_PORT = 30004;
	private static final byte RTDE_DATA_PACKAGE = 0x55;
	private static final byte RTDE_TEXT_MESSAGE = 0x4D;
	private static final byte RTDE_REQUEST_PROTOCOL_VERSION = 0x56;
	private static final byte RTDE_CONTROL_PACKAGE_SETUP_OUTPUTS = 0x4F;
	private static final byte RTDE_CONTROL_PACKAGE_START = 0x53;
	private static final byte RTDE_CONTROL_PACKAGE_PAUSE = 0x50;
	private static final byte POLLING_INTERVAL_MS = 100;
	
	private static RTDEClient rtdeClient;
	private RobotState robotState;
	
	private Socket socket;
	private OutputStream outputStream;
	private InputStream inputStream;
	private boolean running = false;
	
	private static final Logger logger = LoggerFactory.getLogger(RTDEClient.class);
	
	public void connect(String ip) {
		try {
			socket = new Socket(ip, RTDE_PORT);
			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();
			logger.info("RTDE socket connected to remote port: {}", socket.getPort());
			boolean versionOK = negotiateProtocolVersion(inputStream, outputStream, 2);
			if (!versionOK) {
				logger.error("RTDE protocol version negotiation failed. Aborting connection.");
				return;
			}
			logger.info("RTDE protocol version negotiated successfully.");
			
			String[] outputs = {
				"actual_q",
				"actual_TCP_pose"
			};
			boolean setupOK = setupOutputs(outputs, 500.0);
			if (!setupOK) {
				logger.warn("Failed to set up RTDE outputs.");
				return;
			}
			boolean started = startDataStream();
			if (!started) {
				logger.warn("Failed to start RTDE data stream.");
				return;
			}
			// Wait for valid DATA_PACKAGE
			while (true) {
				int b1 = inputStream.read();
				int b2 = inputStream.read();
				int b3 = inputStream.read();
				int size = ((b1 & 0xFF) << 8) | (b2 & 0xFF);
				int type = b3 & 0xFF;
				if (type == RTDE_DATA_PACKAGE) {
					byte[] payload = inputStream.readNBytes(size - 3);
					parseDataPackage(payload);
					break;
				} else {
					inputStream.skipNBytes(size - 3);
				}
			}
			running = true;
			Thread rtdeReaderThread = new Thread(() -> {
				while (running) {
					try {
						readRTDEPacket();
					} catch (Exception e) {
						logger.error("RTDE read error in thread: {}", e.getMessage());
					}
				}
			});
			rtdeReaderThread.setDaemon(true);
			rtdeReaderThread.start();
		} catch (IOException e) {
			logger.error("Connection error: {}", e.getMessage());
		}
	}
	
	public void disconnect() {
		try {
			running = false;
			if (outputStream != null) {
				ByteBuffer buffer = ByteBuffer.allocate(3);
				buffer.order(ByteOrder.BIG_ENDIAN);
				buffer.putShort((short) 3);
				buffer.put(RTDE_CONTROL_PACKAGE_PAUSE);
				outputStream.write(buffer.array());
				outputStream.flush();
				outputStream.close();
			}
			if (inputStream != null)
				inputStream.close();
			if (socket != null)
				socket.close();
			logger.info("RTDE successfully disconnected.");
		} catch (IOException e) {
			logger.error("RTDE problem disconnecting: {}", e.getMessage());
		}
	}
	
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	public static RTDEClient getInstance() {
		if (rtdeClient == null) {
			rtdeClient = new RTDEClient();
		}
		return rtdeClient;
	}
	
	private RTDEClient() {
		super(new Object());
		robotState = new RobotState();
	}
	
	private boolean negotiateProtocolVersion(InputStream in, OutputStream out, int version) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(5);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putShort((short) 5);
		buffer.put((byte) RTDE_REQUEST_PROTOCOL_VERSION);
		buffer.putShort((short) version);
		byte[] packet = buffer.array();
		out.write(packet);
		out.flush();
		// Step 2: Read response header (3 bytes)
		byte[] header = in.readNBytes(3);
		if (header.length < 3) {
			logger.warn("Incomplete header received.");
			return false;
		}
		int size = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);
		int type = header[2] & 0xFF;
		// Step 3: Read the remaining payload
		int payloadLength = size - 3;
		byte[] payload = (payloadLength > 0) ? in.readNBytes(payloadLength) : new byte[0];
		// Step 4: Check for the expected response
		if (type == RTDE_REQUEST_PROTOCOL_VERSION) {
			if (payload.length < 1) {
				logger.warn("RTDE version negotiation response too short");
				return false;
			}
			int accepted = payload[0] & 0xFF;
			return accepted == 1;
		} else if (type == RTDE_TEXT_MESSAGE) {
			String msg = new String(payload, StandardCharsets.UTF_8);
			logger.warn("RTDE_TEXT_MESSAGE: {}", msg);
			return false;
		} else {
			logger.warn(String.format("Unexpected RTDE response type: 0x%02X", type));
			return false;
		}
	}
	
	private boolean setupOutputs(String[] variableNames, double frequency) throws IOException {
		// Frequency (8 bytes)
		ByteBuffer freqBuf = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
		freqBuf.putDouble(frequency);
		byte[] freqBytes = freqBuf.array();
		// Comma-separated variable names
		String joined = String.join(",", variableNames);
		byte[] varBytes = joined.getBytes(StandardCharsets.UTF_8);
		// Combine
		byte[] payload = new byte[freqBytes.length + varBytes.length];
		System.arraycopy(freqBytes, 0, payload, 0, freqBytes.length);
		System.arraycopy(varBytes, 0, payload, freqBytes.length, varBytes.length);
		int length = 3 + payload.length;
		ByteBuffer buffer = ByteBuffer.allocate(length).order(ByteOrder.BIG_ENDIAN);
		buffer.putShort((short) length);
		buffer.put((byte) RTDE_CONTROL_PACKAGE_SETUP_OUTPUTS);
		buffer.put(payload);
		outputStream.write(buffer.array());
		outputStream.flush();
		// Expect a response
		byte[] header = inputStream.readNBytes(3);
		if (header.length < 3) {
			logger.warn("Incomplete response header.");
			return false;
		}
		int size = ((header[0] & 0xFF) << 8) | (header[1] & 0xFF);
		int type = header[2] & 0xFF;
		byte[] response = inputStream.readNBytes(size - 3);
		if (type == RTDE_CONTROL_PACKAGE_SETUP_OUTPUTS) {
			logger.info("RTDE output setup acknowledged with {} bytes.", response.length);
			return true;
		} else {
			logger.warn("Unexpected response type: 0x{}", Integer.toHexString(type));
			return false;
		}
	}
	
	private void readRTDEPacket() {
		try {
			byte[] payload;
			int b1 = inputStream.read();
			int b2 = inputStream.read();
			int b3 = inputStream.read();
			// logger.debug("Raw RTDE header bytes: [{}, {}, {}]", b1, b2, b3) ;
			if (b1 == -1 || b2 == -1 || b3 == -1) {
				logger.warn("RTDE header read incomplete");
				return;
			}
			int size = ((b1 & 0xFF) << 8) | (b2 & 0xFF);
			
			if (size > 1024) {  // Prevent 44,704 byte reads
				logger.error("Invalid RTDE packet size: {}. Aborting read.", size);
				inputStream.skip(size - 3); // optional: clear garbage
				return;
			}
			
			int type = b3 & 0xFF;
			if (size < 3) {
				logger.error("Invalid RTDE packet size: {}", size);
				return;
			}
			switch (type) {
				case RTDE_CONTROL_PACKAGE_START:
					logger.info("Received RTDE_CONTROL_PACKAGE_START confirmation.");
					break;
				case RTDE_DATA_PACKAGE:
					payload = inputStream.readNBytes(size - 3);
					parseDataPackage(payload);
					break;
				case RTDE_TEXT_MESSAGE:
					payload = inputStream.readNBytes(size - 3);
					logger.warn("RTDE message: {}", new String(payload, StandardCharsets.UTF_8));
					break;
				default:
					inputStream.skipNBytes(size - 3);
					logger.debug("Unknown RTDE package type: {}", type);
			}
		} catch (Exception e) {
			logger.error("RTDE read error: {}", e.getMessage());
		}
	}
	
	// private double[] lastActualQ = null;
	// private double[] lastTCPose = null;
	
	private void parseDataPackage(byte[] payload) {
		if (payload.length != 97) {
			logger.error("Unexpected payload length: {} (expected 97)", payload.length);
			return;
		}
		ByteBuffer bb = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN);
		// skip variableId
		bb.get();
		// joints
		double[] actualQ = new double[6];
		for (int i = 0; i < 6; i++)
			actualQ[i] = bb.getDouble();
		
		double[] lastActualQ = robotState.getJointsData().get();
		if (lastActualQ == null || isSignificantlyDifferent(lastActualQ, actualQ, 1e-5)) {
			// lastActualQ = actualQ;
			robotState.getJointsData().set(actualQ);
			firePropertyChange("actual_q", null, actualQ);
		}
		// logger.debug(String.format(">>> RTDE actual_q: %s", java.util.Arrays.toString(actualQ)));
		// TCP pose
		
		double[] tcpPose = new double[6];
		for (int i = 0; i < 6; i++)
			tcpPose[i] = bb.getDouble();
		CartesianData poseData = new CartesianData()
			.setX(tcpPose[0])
			.setY(tcpPose[1])
			.setZ(tcpPose[2])
			.setRx(tcpPose[3])
			.setRy(tcpPose[4])
			.setRz(tcpPose[5]);
		
		// double[] lastTCPose = robotState.getCartesianData().get();
		// logger.debug(String.format(">>> RTDE actual_pose: %s", java.util.Arrays.toString(tcpPose)));
		// double[] lastTCPose = tcpPose;
		
		robotState.setCartesianData(poseData);
		firePropertyChange("actual_pose", null, poseData);
		
		//logger.info(String.format("RTDE actual_pose: %s", java.util.Arrays.toString(tcpPose)));
	}
	
	private boolean isSignificantlyDifferent(double[] a, double[] b, double epsilon) {
		if (a == null || b == null || a.length != b.length) return true;
		for (int i = 0; i < a.length; i++) {
			if (Math.abs(a[i] - b[i]) > epsilon) return true;
		}
		return false;
	}
	
	private boolean startDataStream() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(3);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putShort((short) 3); // Total packet size (3 bytes)
		buffer.put(RTDE_CONTROL_PACKAGE_START);
		byte[] packet = buffer.array();
		outputStream.write(packet);
		outputStream.flush();
		return true;
	}
	
	public RobotState getRobotState() {
		return robotState;
	}
	
	public double[] getJoints() {
		return robotState.getJointsData().get();
	}
	
	// testing method to set joints directly in the RobotState (not sent to robot)
	public void setJoints(double[] jointPositions) {
		System.out.println("here 111 in RTDEClient.setJoints");
		robotState.getJointsData().set(jointPositions);
		firePropertyChange("actual_q", null, jointPositions);
	}
	
	// Denavit–Hartenberg parameters for UR5e (in meters)
	double[] a = {0, 0, 0, 0, 0, 0};
	double[] alpha = {-Math.PI / 2, 0, 0, -Math.PI / 2, Math.PI / 2, 0};
	double[] d = {0.1625, 0, 0.425, 0, 0.3922, 0.09475}; // adjust for UR model
	
	public static Matrix4 computeFlangePose(double[] q) {
		double[] a = {0, 0, 0, 0, 0, 0};
		double[] alpha = {-Math.PI / 2, 0, 0, -Math.PI / 2, Math.PI / 2, 0};
		double[] d = {0.1625, 0, 0.425, 0, 0.3922, 0.09475}; // UR5e
		
		Matrix4 T = Matrix4.identity();
		for (int i = 0; i < 6; i++) {
			T = T.multiply(dhTransformMatrix4(a[i], alpha[i], d[i], q[i]));
		}
		return T;
	}
	
	public static Matrix4 dhTransformMatrix4(double a, double alpha, double d, double theta) {
		double ct = Math.cos(theta);
		double st = Math.sin(theta);
		double ca = Math.cos(alpha);
		double sa = Math.sin(alpha);
		
		double[][] m = {
			{ct, -st * ca, st * sa, a * ct},
			{st, ct * ca, -ct * sa, a * st},
			{0, sa, ca, d},
			{0, 0, 0, 1}
		};
		
		return new Matrix4(m);
	}
	
	public static double[] matrixToPose(Matrix4 T) {
		double x = T.get(0, 3);
		double y = T.get(1, 3);
		double z = T.get(2, 3);
		
		Matrix3x3 R = T.extractRotation(); // assuming Matrix4 → Matrix3x3
		
		double trace = R.get(0, 0) + R.get(1, 1) + R.get(2, 2);
		double angle = Math.acos((trace - 1) / 2);
		double rx = 0, ry = 0, rz = 0;
		
		if (angle > 1e-6) {
			rx = (R.get(2, 1) - R.get(1, 2)) / (2 * Math.sin(angle));
			ry = (R.get(0, 2) - R.get(2, 0)) / (2 * Math.sin(angle));
			rz = (R.get(1, 0) - R.get(0, 1)) / (2 * Math.sin(angle));
			rx *= angle;
			ry *= angle;
			rz *= angle;
		}
		
		return new double[]{x, y, z, rx, ry, rz};
	}
}


