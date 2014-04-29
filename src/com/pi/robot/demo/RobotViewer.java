package com.pi.robot.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.PixelFormat;

import com.pi.robot.physics.BoundingArea;

import com.pi.debug.PILoggerPane;
import com.pi.debug.PIResourceViewer;
import com.pi.math.Vector3D;
import com.pi.robot.Bone;
import com.pi.robot.Skeleton;
import com.pi.robot.overlay.TextOverlay;
import com.pi.robot.physics.AABB;
import com.pi.robot.physics.CompositeArea;
import com.pi.robot.robot.RobotStateManager;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class RobotViewer {
	private static double horizontalTan = Math.tan(Math.toRadians(25));

	private TextOverlay textOverlay = new TextOverlay();
	int width = 1366, height = 768;
	int mode = 2;
	boolean isToggleWireframe;

	public RobotViewer() throws LWJGLException, IOException {
		/*
		 * NetworkTable.setTeam(1983); NetworkTable.setClientMode();
		 * NetworkTable.initialize();
		 */
		NetworkTable.setTeam(1983);
		NetworkTable.setServerMode();
		NetworkTable.initialize();

		PrintStream orig = System.out;
		PIResourceViewer viewer = new PIResourceViewer("Loading");
		PILoggerPane loggerPane = new PILoggerPane();
		PILoggerPane helpPane = new PILoggerPane();
		viewer.addTab("Help", helpPane);
		viewer.addTab("Loading", loggerPane);
		helpPane.getLogOutput().println("J,L: Turns robot");
		helpPane.getLogOutput().println("I,K: Controls drive forward/back");
		helpPane.getLogOutput().println("Y: Third person view");
		helpPane.getLogOutput().println("T: First person view");
		helpPane.getLogOutput().println("Q,E: Controls zoom");
		helpPane.getLogOutput().println("W,A,S,D: Controls camera");
		helpPane.getLogOutput().println();
		helpPane.getLogOutput().println(
				"                         ~By Westin Miller");
		helpPane.getLogOutput().println(
				" --- 3D Robot Visualization for Team 1983 --- ");
		System.setOut(loggerPane.getLogOutput());
		Skeleton sk = new Skeleton(new File("model/mesh.skl"));
		System.setOut(orig);
		viewer.dispose();
		sk.calculate();
		RobotStateManager robot = new RobotStateManager(sk, textOverlay);

		init();

		// CFG Field BB
		Bone bj = sk.getBone(RobotStateManager.FIELD_ID);
		if (bj != null) {
			AABB ob = new AABB(new Vector3D(-140, 0, -305), new Vector3D(140,
					200, 305));
			bj.baseBoundingBox = new CompositeArea(new AABB(new Vector3D(
					ob.min.x - 100, ob.min.y, ob.min.z), new Vector3D(ob.min.x,
					ob.max.y, ob.max.z)), new AABB(new Vector3D(ob.max.x,
					ob.min.y, ob.min.z), new Vector3D(ob.max.x + 100, ob.max.y,
					ob.max.z)),
					new AABB(new Vector3D(ob.min.x, ob.min.y, ob.min.z - 100),
							new Vector3D(ob.max.x, ob.max.y, ob.min.z)),
					new AABB(new Vector3D(ob.min.x, ob.min.y, ob.max.z),
							new Vector3D(ob.max.x, ob.max.y, ob.max.z + 100)));
		}

		Camera3rdPerson cam = new Camera3rdPerson();

		float robotVel = 0;

		long lastDriveTime = 0;

		boolean first = false;

		while (!Display.isCloseRequested()) {
			GL11.glClearColor(1f, 1f, 1f, 1f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			double aspect = (double) height / (double) width;
			GL11.glFrustum(-horizontalTan, horizontalTan, aspect
					* -horizontalTan, aspect * horizontalTan, 1, 100000);
			GL11.glViewport(0, 0, width, height);

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();

			if (first) {
				GL11.glRotatef(10, 1, 0, 0);
				GL11.glTranslatef(0, 0, -35);
				GL11.glTranslatef(10, 0, 0);
				GL11.glRotatef(-robot.yaw + 90, 0, 1, 0);
				GL11.glTranslatef(-robot.x, 0, robot.y);
			} else {
				cam.translate();
				GL11.glTranslatef(-robot.x, 0, robot.y);
			}
			GL11.glTranslatef(0, -25, 0);
			Lighting.apply();

			if (mode == 1) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			} else if (mode == 2) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			} else {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT);
			}
			for (Bone b : sk.getRootBone()) {
				drawBone(b);
			}
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			// for (Bone b : sk.getRootBone()) {
			// boneBB(b);
			// }

			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

			overlay();
			
			robot.doStuff();
			System.out.println(sk.getBone(RobotStateManager.BALL_ID).additional);
			
			Display.update();
			Display.sync(60);
			robot.update();
			Display.setTitle("Robot");
			if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
				if (!isToggleWireframe) {
					mode++;
					if (mode > 2) {
						mode = 0;
					}
				}
				isToggleWireframe = true;
			} else {
				isToggleWireframe = false;
			}

			float tYaw = robot.yaw;
			if (Keyboard.isKeyDown(Keyboard.KEY_J)) {
				tYaw += robotVel;
				lastDriveTime = System.currentTimeMillis();
			} else if (Keyboard.isKeyDown(Keyboard.KEY_L)) {
				tYaw -= robotVel;
				lastDriveTime = System.currentTimeMillis();
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_I)) {
				robotVel++;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
				robotVel--;
			} else {
				robotVel *= 0.75f;
				if (Math.abs(robotVel) < 0.4f) {
					robotVel = 0;
				}
			}
			float tX = robot.x
					+ (float) (robotVel * Math.cos(Math.toRadians(tYaw)));
			float tY = robot.y
					+ (float) (robotVel * Math.sin(Math.toRadians(tYaw)));
			boolean quit = true;
			do {
				if (!robot.robotCollides(tX, tY, tYaw)) {
					break;
				} else if (!robot.robotCollides(tX, robot.y, tYaw)) {
					tY = robot.y;
					tYaw += robotVel * Math.cos(Math.toRadians(tYaw));
				} else if (!robot.robotCollides(robot.x, tY, tYaw)) {
					tX = robot.x;
					tYaw += robotVel * Math.sin(Math.toRadians(tYaw));
				} else {
					tY = robot.y;
					tYaw = robot.yaw;
					tX = robot.x;
				}
			} while (!quit);
			if (tX != robot.x || tY != robot.y || tYaw != robot.yaw) {
				lastDriveTime = System.currentTimeMillis();
			}
			robot.x = tX;
			robot.y = tY;
			robot.yaw = tYaw;

			if (Keyboard.isKeyDown(Keyboard.KEY_T)) {
				first = true;
			} else if (Keyboard.isKeyDown(Keyboard.KEY_Y)) {
				first = false;
			}
			robot.driveMode = System.currentTimeMillis() - lastDriveTime < 10000;
			cam.stalled = !robot.driveMode;
			if (Math.abs(robotVel) > 3) {
				robotVel = Math.signum(robotVel) * 3;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				break;
			}
		}
		for (Bone b : sk.getRootBone()) {
			unloadBone(b);
		}
		Lighting.takedown();
	}

	private void overlay() {
		GL20.glUseProgram(0);
		double aspect = ((double) height) / ((double) width);
		// Overlay
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glNormal3f(0, 0, 0);
		GL11.glTranslatef(-(float) horizontalTan,
				(float) (horizontalTan * aspect), -1);
		GL11.glScalef((float) horizontalTan / width * 2f,
				-(float) horizontalTan / width * 2f, 1);
		// GL11.glRotatef(180, 1, 0, 0);
		textOverlay.renderOverlay(width, height);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	private void renderBB(BoundingArea bb) {
		if (bb instanceof AABB) {
			AABB ab = (AABB) bb;
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex3f(ab.max.x, ab.min.y, ab.max.z);
			GL11.glVertex3f(ab.max.x, ab.min.y, ab.min.z);
			GL11.glVertex3f(ab.max.x, ab.max.y, ab.min.z);
			GL11.glVertex3f(ab.max.x, ab.max.y, ab.max.z);

			GL11.glVertex3f(ab.max.x, ab.max.y, ab.max.z);
			GL11.glVertex3f(ab.max.x, ab.max.y, ab.min.z);
			GL11.glVertex3f(ab.min.x, ab.max.y, ab.min.z);
			GL11.glVertex3f(ab.min.x, ab.max.y, ab.max.z);

			GL11.glVertex3f(ab.max.x, ab.max.y, ab.max.z);
			GL11.glVertex3f(ab.min.x, ab.max.y, ab.max.z);
			GL11.glVertex3f(ab.min.x, ab.min.y, ab.max.z);
			GL11.glVertex3f(ab.max.x, ab.min.y, ab.max.z);

			GL11.glVertex3f(ab.min.x, ab.min.y, ab.max.z);
			GL11.glVertex3f(ab.min.x, ab.max.y, ab.max.z);
			GL11.glVertex3f(ab.min.x, ab.max.y, ab.min.z);
			GL11.glVertex3f(ab.min.x, ab.min.y, ab.min.z);

			GL11.glVertex3f(ab.min.x, ab.min.y, ab.max.z);
			GL11.glVertex3f(ab.min.x, ab.min.y, ab.min.z);
			GL11.glVertex3f(ab.max.x, ab.min.y, ab.min.z);
			GL11.glVertex3f(ab.max.x, ab.min.y, ab.max.z);

			GL11.glVertex3f(ab.min.x, ab.min.y, ab.min.z);
			GL11.glVertex3f(ab.min.x, ab.max.y, ab.min.z);
			GL11.glVertex3f(ab.max.x, ab.max.y, ab.min.z);
			GL11.glVertex3f(ab.max.x, ab.min.y, ab.min.z);
			GL11.glEnd();
		} else if (bb instanceof CompositeArea) {
			for (BoundingArea j : ((CompositeArea) bb).areas) {
				renderBB(j);
			}
		}
	}

	public void boneBB(Bone b) {
		if (b.boundingBox != null) {
			renderBB(b.boundingBox);
		}
		for (Bone c : b.getChildren()) {
			boneBB(c);
		}
	}

	private void unloadBone(Bone b) {
		if (b.mesh != null) {
			b.mesh.unloadFromGPU();
		}
		for (Bone s : b.getChildren()) {
			drawBone(s);
		}
	}

	private void drawBone(Bone b) {
		b.draw();
		for (Bone s : b.getChildren()) {
			drawBone(s);
		}
	}

	private void init() throws LWJGLException {
		// Display.setDisplayModeAndFullscreen(new DisplayMode(1366,768));
		Display.setDisplayMode(new DisplayMode(width, height));
		Display.create(new PixelFormat(0, 8, 0, 4));

		Lighting.setup();

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void main(String[] args) throws LWJGLException, IOException {
		new RobotViewer();
	}
}
