package com.pi.robot.demo;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class Camera3rdPerson {
	private static final float yawMilli = .1f;
	private static final float yawStalledMilli = .01f;
	private static final float pitchMilli = .1f;
	private static final float centerMilli = .1f;

	private float pitch = 10, yaw = 0, centerDist = 100;
	private long lastMoveProc = -1;

	public void modPitch(float val) {
		if (pitch + val >= 0 && pitch + val <= 90) {
			pitch += val;
		}
	}

	public void modYaw(float val) {
		yaw += val;
		if (yaw < 0)
			yaw += 360;
		if (yaw >= 360)
			yaw -= 360;
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}
	
	public boolean stalled = true;

	public void translate() {
		if (lastMoveProc != -1) {
			long passed = System.currentTimeMillis() - lastMoveProc;
			if (Keyboard.isKeyDown(Keyboard.KEY_W))
				modPitch(passed * pitchMilli);
			else if (Keyboard.isKeyDown(Keyboard.KEY_S))
				modPitch(-passed * pitchMilli);
			if (Keyboard.isKeyDown(Keyboard.KEY_A))
				modYaw(-passed * yawMilli);
			else if (Keyboard.isKeyDown(Keyboard.KEY_D))
				modYaw(passed * yawMilli);
			if (Keyboard.isKeyDown(Keyboard.KEY_Q))
				centerDist -= passed * centerMilli;
			else if (Keyboard.isKeyDown(Keyboard.KEY_E))
				centerDist += passed * centerMilli;
			if (stalled)
				modYaw(yawStalledMilli * passed);
		}
		lastMoveProc = System.currentTimeMillis();
		GL11.glTranslatef(0, 0, -centerDist);
		GL11.glRotatef(pitch, 1, 0, 0);
		GL11.glRotatef(360 - yaw, 0, 1, 0);
	}

	@Override
	public String toString() {
		return "3rd Person Camera: Yaw: " + yaw + " Pitch: " + pitch + " Mag: "
				+ centerDist;
	}
}