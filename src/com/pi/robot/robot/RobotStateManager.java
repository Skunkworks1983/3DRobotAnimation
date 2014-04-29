package com.pi.robot.robot;

import java.awt.Color;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;

import com.pi.math.Quaternion;
import com.pi.math.TransMatrix;
import com.pi.math.Vector3D;
import com.pi.robot.Bone;
import com.pi.robot.Skeleton;
import com.pi.robot.mesh.FloatBufferColor;
import com.pi.robot.mesh.Mesh;
import com.pi.robot.overlay.TextOverlay;
import com.pi.robot.overlay.TextOverlay.Corner;
import com.pi.robot.overlay.TimedMessage;
import com.pi.robot.physics.BallController;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class RobotStateManager {
	public static final int BALL_ID = 5;
	public static final int CATAPULT_ID = 4;
	public static final int JAWS_ID = 3;
	public static final int PTERODACTYL_ID = 2;
	public static final int DRIVEBASE_ID = 0;
	public static final int TOPPER_ID = 1;
	public static final int FIELD_ID = 6;

	public static final FloatBufferColor defaultColor = new FloatBufferColor(
			0.75f, 0.75f, 0.75f);
	private static final FloatBufferColor NOTIFICATION_STALL_COLOR = new FloatBufferColor(
			1f, 0f, 0f, 0.125f);

	private Skeleton sk;
	private ConsoleListener listener;

	public Changeable<Alliance> alliance = new Changeable<Alliance>(
			Alliance.INVALID);
	public boolean hasBall;
	public boolean jawsClosed;

	public float pterodactylAngle = 0;
	public Changeable<MotorState> collectorState = new Changeable<MotorState>(
			MotorState.OFF);

	public float shooterAngle = 0;
	public Changeable<MotorState> winchState = new Changeable<MotorState>(
			MotorState.OFF);

	public Changeable<MotorState> compressorState = new Changeable<MotorState>(
			MotorState.OFF);
	public Changeable<MotorState> driveLeftState = new Changeable<MotorState>(
			MotorState.OFF);
	public Changeable<MotorState> driveRightState = new Changeable<MotorState>(
			MotorState.OFF);

	public Vector3D winchNotificationPos = new Vector3D(-0.625f, 0.125f, -3.0f);

	private float currentPterodactylAngle = 0;
	private float targetJawsAngle = 0;
	private float currentJawsAngle = 0;
	private NetworkTable table;

	public float x, y, yaw = 180;
	public boolean driveMode;
	public BallController ballController;

	public RobotStateManager(Skeleton sk, final TextOverlay textOverlay) {
		this.sk = sk;
		table = NetworkTable.getTable("Robot");
		ballController = new BallController(sk);
		(new Thread(new Runnable() {

			@Override
			public void run() {
				long last = System.currentTimeMillis();
				while (true) {
					long delta = System.currentTimeMillis() - last;
					ballController.stepControl((float) delta / 1000f);
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
			}
		})).start();
		// DemoMode.startDemoMode(table, sk, ballController);
		textOverlay.setCornerSize(Corner.UP_RIGHT, 5);
		try {
			listener = new ConsoleListener(1983);
			new Thread(new Runnable() {
				StringBuffer buffer = new StringBuffer();

				public void textCallback(String s) {
					if (s.toLowerCase().contains("watchdog")) {
						textOverlay.addMessage(Corner.UP_LEFT,
								new TimedMessage(s).setColor(Color.RED));
					} else {
						textOverlay.addMessage(Corner.UP_RIGHT,
								new TimedMessage(s, 1000));
					}
					System.out.println(s);
				}

				public void run() {
					while (true) {
						try {
							buffer.append(listener.read());
						} catch (IOException e) {
							e.printStackTrace();
						}
						int idx;
						while ((idx = buffer.indexOf("\n")) > 0) {
							textCallback(buffer.substring(0, idx));
							buffer.delete(0, idx + 1);
						}
						try {
							Thread.sleep(100L);
						} catch (InterruptedException e) {
						}
					}
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void doStuff() {
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			ballController.launch(new Vector3D(10, 10, 10));
		}
	}

	public boolean robotCollides(float x, float y, float yaw) {
		Bone driveBase = sk.getBone(DRIVEBASE_ID);
		Bone field = sk.getBone(FIELD_ID);
		if (driveBase == null || field == null) {
			return false;
		}
		driveBase.base.set(x, 0, -y);
		driveBase.setYPR(((float) (yaw * Math.PI) / 180f), 0, 0);
		driveBase.calculateRecursive();
		boolean res = driveBase.collidesRecursive(field.boundingBox, FIELD_ID,
				BALL_ID);
		driveBase.base.set(this.x, 0, -this.y);
		driveBase.setYPR(((float) (this.yaw * Math.PI) / 180f), 0, 0);
		return res;
	}

	private void colorAlliance(FloatBufferColor color) {
		if (sk.getBone(BALL_ID) != null) {
			Bone bone = sk.getBone(BALL_ID);
			Mesh mesh = bone.mesh;
			FloatBuffer vBuff = mesh.vertexBuffer;
			if (mesh.colorBuffer == null) {
				mesh.colorBuffer = BufferUtils
						.createFloatBuffer(vBuff.limit() * 4);
			}
			FloatBuffer cBuff = mesh.colorBuffer;
			for (int i = 0; i < vBuff.limit() / 3; i++) {
				int c = i * 4;
				cBuff.put(c, color.getBuffer().get(0));
				cBuff.put(c + 1, color.getBuffer().get(1));
				cBuff.put(c + 2, color.getBuffer().get(2));
				cBuff.put(c + 3, color.getBuffer().get(3));
			}
			mesh.loadToGPU();
		}
	}

	private void colorMesh(FloatBufferColor motorColor, Mesh mesh,
			Vector3D min, Vector3D max, TransMatrix bleh) {
		Vector3D base = min.clone().add(max).multiply(.5f);
		min.subtract(base);
		max.subtract(base);
		if (mesh != null) {
			FloatBuffer vBuff = mesh.vertexBuffer;
			FloatBuffer cBuff = mesh.colorBuffer;
			for (int i = 0; i < vBuff.limit() / 3; i++) {
				int v = i * 3;
				int c = i * 4;
				Vector3D test = new Vector3D(vBuff.get(v), vBuff.get(v + 1),
						vBuff.get(v + 2)).subtract(base);
				if (bleh != null) {
					test = bleh.multiply(test);
				}
				if (test.inside(min, max)) {
					cBuff.put(c, motorColor.getBuffer().get(0));
					cBuff.put(c + 1, motorColor.getBuffer().get(1));
					cBuff.put(c + 2, motorColor.getBuffer().get(2));
					cBuff.put(c + 3, motorColor.getBuffer().get(3));
				}
			}
			mesh.loadToGPU();
		}
	}

	private void colorWinchMotor(FloatBufferColor motorColor) {
		if (sk.getBone(PTERODACTYL_ID) == null
				|| sk.getBone(PTERODACTYL_ID).mesh == null) {
			return;
		}
		Vector3D min = new Vector3D(-2.75f, -.75f, -6f);
		Vector3D max = new Vector3D(1.5f, 1f, 0f);
		colorMesh(motorColor, sk.getBone(PTERODACTYL_ID).mesh, min, max, null);
	}

	private void colorCollectorMotor(FloatBufferColor motorColor) {
		if (sk.getBone(JAWS_ID) == null) {
			return;
		}
		Vector3D min = new Vector3D(-3.91f, -6.1f, 24f);
		Vector3D max = new Vector3D(0f, 6.1f, 25.76f);
		TransMatrix bleh = new TransMatrix().setRotation(0f, 1f, 0f, -33f
				* (float) Math.PI / 180.0f);
		colorMesh(motorColor, sk.getBone(JAWS_ID).mesh, min, max, bleh);
	}

	private void colorCompressor(FloatBufferColor compressorColor) {
		if (sk.getBone(DRIVEBASE_ID) == null
				|| sk.getBone(DRIVEBASE_ID).mesh == null) {
			return;
		}
		Vector3D min = new Vector3D(6, -1.1f, 1.2f);
		Vector3D max = new Vector3D(11.5f, 1.1f, 6f);
		colorMesh(compressorColor, sk.getBone(DRIVEBASE_ID).mesh, min, max,
				null);
	}

	private void colorDriveMotor(FloatBufferColor compressorColor, boolean left) {
		if (sk.getBone(DRIVEBASE_ID) == null
				|| sk.getBone(DRIVEBASE_ID).mesh == null) {
			return;
		}
		Vector3D min = new Vector3D(4, left ? 1.4f : -6f, 3.5f);
		Vector3D max = new Vector3D(11.5f, left ? 6f : -1.4f, 8);
		colorMesh(compressorColor, sk.getBone(DRIVEBASE_ID).mesh, min, max,
				null);
	}

	private void updateJaws() {
		if (sk.getBone(JAWS_ID) == null) {
			return;
		}
		if (sk.getBone(BALL_ID) == null) {
			return;
		}

		if (jawsClosed) {
			float adj = -(float) Math
					.sin((20.0f - sk.getBone(BALL_ID).additional.x) / 20.0f
							* Math.PI)
					* ((float) Math.PI * 0.1f);
			targetJawsAngle = hasBall
					&& sk.getBone(BALL_ID).additional.x < 20.0f ? (float) (Math.PI / 8f)
					- adj
					: (float) Math.PI / 10f;
		} else {
			targetJawsAngle = (float) Math.PI / 5f;
		}

		if (hasBall && currentJawsAngle < (float) Math.PI / 8f) {
			currentJawsAngle = (float) Math.PI / 8f;
		}

		sk.getBone(JAWS_ID).slerp(new Quaternion(0f, (float) 0f, 0f),
				new Quaternion(0.0f, (float) -1.0f, 0.0f), currentJawsAngle);
		float diffA = Math.signum(targetJawsAngle - currentJawsAngle) * 0.1f;
		if (diffA > 0) {
			diffA *= 0.05f;
		}
		float diffB = targetJawsAngle - currentJawsAngle;
		currentJawsAngle += Math.abs(diffA) < Math.abs(diffB) ? diffA : diffB;
	}

	private void updatePterodactyl() {
		if (sk.getBone(PTERODACTYL_ID) == null) {
			return;
		}

		sk.getBone(PTERODACTYL_ID).slerp(new Quaternion(0f, (float) 0f, 0f),
				new Quaternion(0.0f, (float) 1.0f, 0.0f),
				(float) (Math.PI / 1.75) - currentPterodactylAngle);
		float diffA = Math.signum(pterodactylAngle - currentPterodactylAngle) * 0.05f;
		float diffB = pterodactylAngle - currentPterodactylAngle;
		currentPterodactylAngle += Math.abs(diffA) < Math.abs(diffB) ? diffA
				: diffB;
	}

	private void updateMotors() {
		if (collectorState.hasChanged()) {
			colorCollectorMotor(collectorState.getState().getColor());
		}
		if (winchState.hasChanged() && sk.getBone(PTERODACTYL_ID) != null) {
			colorWinchMotor(winchState.getState().getColor());
			if (winchState.getState() == MotorState.STALLED) {
				// Icky icky TODO
				sk.getBone(PTERODACTYL_ID).notifications.put(
						winchNotificationPos, new NotificationBubble(
								winchNotificationPos, NOTIFICATION_STALL_COLOR,
								5));
			} else {
				sk.getBone(PTERODACTYL_ID).notifications
						.remove(winchNotificationPos);
			}
		}
	}

	private void updateShooter() {
		if (sk.getBone(CATAPULT_ID) != null) {
			sk.getBone(CATAPULT_ID).slerp(new Quaternion(0f, (float) 0f, 0f),
					new Quaternion(0.0f, (float) 1.0f, 0.0f),
					shooterAngle - (float) Math.PI / 3f - (float) Math.PI / 6f);
		}
	}

	public void update() {
		hasBall = table.getBoolean("hasBall", false);
		jawsClosed = table.getBoolean("jawsClosed", false);
		pterodactylAngle = (float) (table.getNumber("pterodactylAngle", 0)
				* Math.PI / 180.0f);
		collectorState.setState(MotorState.decode((int) table.getNumber(
				"collectorMotorState", 0)));
		alliance.setState(Alliance.decode((int) table.getNumber("alliance", 2)));

		shooterAngle = table.getBoolean("shooterLatched", false) ? 0
				: (float) table.getNumber("shooterStrap", 0) * (float) Math.PI
						/ 2.0f;
		winchState.setState(MotorState.decode((int) table.getNumber(
				"winchMotorState", 0)));

		compressorState.setState(MotorState.decode((int) table.getNumber(
				"compressorState", 0)));
		driveLeftState.setState(MotorState.decode((int) table.getNumber(
				"driveLeftState", 0)));
		driveRightState.setState(MotorState.decode((int) table.getNumber(
				"driveRightState", 0)));

		if (!driveMode) {
			yaw = (float) table.getNumber("YAW", 0);
			x = (float) table.getNumber("DBX", 0);
			y = (float) table.getNumber("DBY", 0);
		}

		if (sk.getBone(BALL_ID) != null) {
			sk.getBone(BALL_ID).visible = hasBall;
		}
		updateJaws();
		updatePterodactyl();
		updateMotors();
		updateShooter();
		updateDrivebase();
		sk.calculate();

		if (alliance.hasChanged()) {
			colorAlliance(alliance.getState().getColor());
		}
	}

	private void updateDrivebase() {
		if (compressorState.hasChanged()) {
			colorCompressor(compressorState.getState().getColor());
		}
		if (driveLeftState.hasChanged()) {
			colorDriveMotor(driveLeftState.getState().getColor(), true);
		}
		if (driveRightState.hasChanged()) {
			colorDriveMotor(driveRightState.getState().getColor(), false);
		}
		if (sk.getBone(DRIVEBASE_ID) != null) {
			sk.getBone(DRIVEBASE_ID).base.set(x, 0, -y);
			sk.getBone(DRIVEBASE_ID).setYPR(((float) (yaw * Math.PI) / 180f),
					0, 0);
		}
	}
}
