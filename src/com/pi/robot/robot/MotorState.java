package com.pi.robot.robot;

import com.pi.robot.mesh.FloatBufferColor;

public enum MotorState {
	OFF(new FloatBufferColor(0.85f, 0.85f, 0.85f)), RUNNING(
			new FloatBufferColor(.25f, 1f, 0.25f)), STALLED(
			new FloatBufferColor(1f, 0.25f, 0.25f));
	private FloatBufferColor fb;

	private MotorState(FloatBufferColor fb) {
		this.fb = fb;
	}

	public FloatBufferColor getColor() {
		return fb;
	}

	public static MotorState decode(int i) {
		switch (i) {
		case 2:
			return STALLED;
		case 1:
			return RUNNING;
		case 0:
		default:
			return OFF;
		}
	}
}
