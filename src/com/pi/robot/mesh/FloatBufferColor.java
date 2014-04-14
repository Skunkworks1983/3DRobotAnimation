package com.pi.robot.mesh;

import java.awt.Color;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public final class FloatBufferColor {
	private FloatBuffer buffer = BufferUtils.createFloatBuffer(4);

	public FloatBufferColor() {
		this(0, 0, 0, 255);
	}

	public FloatBufferColor(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public FloatBufferColor(float r, float g, float b) {
		this(r, g, b, 1);
	}

	public FloatBufferColor(int r, int g, int b, int a) {
		set(r, g, b, a);
	}

	public FloatBufferColor(float r, float g, float b, float a) {
		set(r, g, b, a);
	}

	public FloatBufferColor(Color c) {
		setColor(c);
	}

	public void set(int r, int g, int b, int a) {
		set(((float) r) / 255f, ((float) g) / 255f, ((float) b) / 255f,
				((float) a) / 255f);
	}

	public void set(float r, float g, float b, float a) {
		buffer.put(0, r);
		buffer.put(1, g);
		buffer.put(2, b);
		buffer.put(3, a);
	}

	public void set(int r, int g, int b) {
		set(r, g, b, 255);
	}

	public void set(float r, float g, float b) {
		set(r, g, b, 1);
	}

	public void setColor(Color src) {
		set(src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha());
	}

	public FloatBuffer getBuffer() {
		return (FloatBuffer) buffer.rewind();
	}

	public boolean equals(Object o) {
		if (o instanceof FloatBufferColor) {
			FloatBufferColor fb = (FloatBufferColor) o;
			return fb.buffer.get(0) == buffer.get(0)
					&& fb.buffer.get(1) == buffer.get(1)
					&& fb.buffer.get(2) == buffer.get(2)
					&& fb.buffer.get(3) == buffer.get(3);
		}
		return false;
	}

	public String toString() {
		return getBuffer().get() + "," + getBuffer().get() + ","
				+ getBuffer().get() + "," + getBuffer().get();
	}
}
