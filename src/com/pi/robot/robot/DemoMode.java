package com.pi.robot.robot;

import com.pi.robot.physics.BallController;

import com.pi.math.BezierCurve;
import com.pi.math.Vector3D;
import com.pi.robot.Bone;
import com.pi.robot.Skeleton;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class DemoMode {
	public static final float SHOOT_X = 48.8f;
	public static final float SHOOT_Y = 195.62f;
	public static final float SHOOT_YAW = 63.0f;

	public static final float TURN_X = 0.0f;
	public static final float TURN_Y = 0.0f;
	public static final float TURN_YAW = 0f;

	public static final float COLLECT_X = 88.0f;
	public static final float COLLECT_Y = -236.8f;
	public static final float COLLECT_YAW = -15.0f;

	public static BezierCurve turnToCollect = null;
	public static BezierCurve turnToShoot = null;

	static {
		float turnWeight = 25;
		float shootTurnWeight = 25;
		Vector3D[] turn = {
				new Vector3D(TURN_X, TURN_Y, 0),
				new Vector3D(TURN_X + turnWeight
						* (float) Math.cos(Math.toRadians(TURN_YAW)), TURN_Y
						+ turnWeight
						* (float) Math.sin(Math.toRadians(TURN_YAW)), 0) };

		Vector3D[] collect = {
				new Vector3D(COLLECT_X, COLLECT_Y, 0),
				new Vector3D(
						COLLECT_X + turnWeight
								* (float) Math.cos(Math.toRadians(COLLECT_YAW)),
						COLLECT_Y + turnWeight
								* (float) Math.sin(Math.toRadians(COLLECT_YAW)),
						0) };

		Vector3D[] shoot = {
				new Vector3D(SHOOT_X, SHOOT_Y, 0),
				new Vector3D(SHOOT_X + shootTurnWeight
						* (float) Math.cos(Math.toRadians(SHOOT_YAW)), SHOOT_Y
						+ shootTurnWeight
						* (float) Math.sin(Math.toRadians(SHOOT_YAW)), 0) };

		turnToCollect = new BezierCurve(new Vector3D[] { turn[0], turn[1],
				collect[1], collect[0] }, .005D);
		turnToShoot = new BezierCurve(new Vector3D[] { turn[0], turn[1],
				shoot[1], shoot[0] }, .005D);

	}

	public static void traverseCurve(NetworkTable table, BezierCurve c,
			int stepSize) {
		for (int i = (stepSize < 0 ? c.getAngles().length - 1 : 0); (stepSize < 0 ? i >= 0
				: i < c.getAngles().length); i += stepSize) {
			table.putNumber("YAW", Math.toDegrees(c.getAngles()[i]));
			table.putNumber("DBX", c.getPoints()[i].x);
			table.putNumber("DBY", c.getPoints()[i].y);
			try {
				Thread.sleep(10L);
			} catch (InterruptedException e) {
			}
		}
	}

	public static void movePtero(NetworkTable table, float target)
			throws Exception {
		double curr = table.getNumber("pterodactylAngle", 0);
		do {
			double next = curr
					+ (Math.abs(target - curr) > 2.5 ? (Math.signum(target
							- curr) * 2.5) : (target - curr));
			table.putNumber("pterodactylAngle", next);
			curr = next;
			Thread.sleep(50L);
		} while (Math.abs(curr - target) > 2.5);
	}

	public static void moveShooter(NetworkTable table, float target)
			throws Exception {
		double curr = table.getNumber("shooterStrap", 0);
		do {
			double next = curr
					+ (Math.abs(target - curr) > 0.025 ? Math.signum(target
							- curr) * 0.025 : (target - curr));
			table.putNumber("shooterStrap", next);
			curr = next;
			Thread.sleep(50L);
		} while (Math.abs(curr - target) > 0.03);
	}

	public static void tweenBall(Skeleton sk, Vector3D target, float speed)
			throws InterruptedException {
		Bone ball = sk.getBone(5);
		if (ball != null) {
			ball.setAttached(true);
			do {
				Vector3D dir = target.clone().subtract(ball.additional);
				if (dir.magnitude() > speed) {
					dir.normalize().multiply(speed);
				}
				ball.additional.add(dir);
				Thread.sleep(50L);
			} while (Math.abs(ball.additional.x - target.x) > 0.01
					|| Math.abs(ball.additional.y - target.y) > 0.01
					|| Math.abs(ball.additional.z - target.z) > 0.01);
		}
	}

	public static void startDemoMode(final NetworkTable table,
			final Skeleton sk, final BallController ballController) {
		new Thread(new Runnable() {
			public void run() {
				final Bone ball = sk.getBone(5);
				Runnable resetShooter = new Runnable() {
					public void run() {
						try {
							moveShooter(table, 0);
						} catch (Exception e) {
						}
					}
				};
				Runnable zeroPtero = new Runnable() {
					public void run() {
						try {
							movePtero(table, 0);
						} catch (Exception e) {
						}
					}
				};
				Runnable ninePtero = new Runnable() {
					public void run() {
						try {
							movePtero(table, 90);
						} catch (Exception e) {
						}
					}
				};
				Runnable sevenPtero = new Runnable() {
					public void run() {
						try {
							movePtero(table, 75);
						} catch (Exception e) {
						}
					}
				};
				Runnable shootBall = new Runnable() {
					public void run() {
						try {
							table.putBoolean("hasBall", true);
							ball.visible = true;
							ballController.launch(new Vector3D(25f, 25f,
									25F));
							Thread.sleep(750L);
							Thread.sleep(250L);
						} catch (InterruptedException e) {
						}
					}
				};
				while (true) {
					try {
						table.putNumber("alliance",
								((int) table.getNumber("alliance", 0)) ^ 0);
						// Reset shooter
						System.out.println("resetting shooter");
						Thread rShot = new Thread(resetShooter);
						rShot.start();
						table.putBoolean("shooterLatched", true);
						table.putNumber("shooterStrap", 1.1);

						traverseCurve(table, turnToShoot, -1);

						// Collect pos
						System.out.println("Move ptero");
						Thread zPtero = new Thread(zeroPtero);
						zPtero.start();

						traverseCurve(table, turnToCollect, 1);
						table.putNumber("YAW", COLLECT_YAW);
						table.putNumber("DBX", COLLECT_X);
						table.putNumber("DBY", COLLECT_Y);
						rShot.join(1000);
						zPtero.join(1000);

						// Collect
						System.out.println("Collect");
						table.putNumber("collectorMotorState", 1);
						table.putBoolean("jawsClosed", true);

						Thread.sleep(1500);
						table.putNumber("collectorMotorState", 0);
						if (ball != null) {
							ball.additional = new Vector3D(30, 0, 0);
						}
						table.putBoolean("hasBall", true);
						tweenBall(sk, new Vector3D(0f, 0f, 0f), 1.5f);

						// Carry pos
						System.out.println("Move ptero");
						Thread nPtero = new Thread(ninePtero);
						nPtero.start();

						// Move the drivebase
						table.putNumber("driveLeftState", 1);
						table.putNumber("driveRightState", 1);
						traverseCurve(table, turnToCollect, -1);
						traverseCurve(table, turnToShoot, 1);
						table.putNumber("YAW", SHOOT_YAW);
						table.putNumber("DBX", SHOOT_X);
						table.putNumber("DBY", SHOOT_Y);
						// Thread.sleep(1500);
						nPtero.join(1000);

						// Shoot pos
						table.putNumber("driveLeftState", 0);
						table.putNumber("driveRightState", 0);
						sevenPtero.run();
						table.putBoolean("jawsClosed", false);
						Thread.sleep(100L);
						table.putBoolean("shooterLatched", true);
						table.putNumber("shooterStrap", 1.1);
						table.putBoolean("shooterLatched", false);
						// Thread shootDaShot = new Thread(shootBall);
						// shootDaShot.start();
						shootBall.run();
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}
}
