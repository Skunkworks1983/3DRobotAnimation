package com.pi.robot.overlay;

import java.awt.Color;

public class TimedMessage {
	private static final long MESSAGE_TIME = 2000L;

	private String msg;
	private long time;
	private long life;
	private Color color = Color.WHITE;

	public TimedMessage(String s) {
		this(s, MESSAGE_TIME);
	}

	public TimedMessage(String s, long life) {
		this.msg = s;
		this.time = System.currentTimeMillis();
		this.life = life;
	}

	public TimedMessage setColor(Color color) {
		this.color = color;
		return this;
	}

	public Color getColor() {
		return color;
	}

	public boolean isDead() {
		return life > 0 && System.currentTimeMillis() - time > life;
	}

	public void kill() {
		this.time = 0;
		this.life = 0;
	}

	public String getMessage() {
		return msg;
	}
}
