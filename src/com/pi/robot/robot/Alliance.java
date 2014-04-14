package com.pi.robot.robot;

import com.pi.robot.mesh.FloatBufferColor;

public enum Alliance {
	RED(new FloatBufferColor(1f, .25f, .25f)), BLUE(new FloatBufferColor(.25f,
			.25f, 1f)), INVALID(new FloatBufferColor(.25f, 1f, .25f));
	private FloatBufferColor fb;

	private Alliance(FloatBufferColor fb) {
		this.fb = fb;
	}

	public FloatBufferColor getColor() {
		return fb;
	}

	public static Alliance decode(int i) {
		switch (i) {
		case 0:
			return RED;
		case 1:
			return BLUE;
		case 2:
		default:
			return INVALID;
		}
	}
}
