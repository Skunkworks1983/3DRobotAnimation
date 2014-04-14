package com.pi.robot.robot;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import com.pi.math.Vector3D;
import com.pi.robot.mesh.FloatBufferColor;

public class NotificationBubble {
	private static final double EXPAND_TIME = 1000.0;
	private Vector3D center;
	private FloatBufferColor color;
	private long startTime;
	private float size;

	public NotificationBubble(Vector3D center, FloatBufferColor color,
			float size) {
		this.center = center;
		this.color = color;
		this.size = size;
		this.startTime = System.currentTimeMillis();
	}

	public void render() {
		GL11.glBegin(GL11.GL_QUADS);
		FloatBuffer color = this.color.getBuffer();
		GL11.glColor4f(color.get(), color.get(), color.get(), color.get());
		float radius = size;
		double time = System.currentTimeMillis() - startTime;
		if (time < EXPAND_TIME) {
			radius *= (time / EXPAND_TIME);
		} else {
			radius += Math.sin(Math.sqrt(EXPAND_TIME / time)
					* (time - EXPAND_TIME) * 10.0 / EXPAND_TIME)
					* (size / 10.0) * Math.sqrt(EXPAND_TIME / time);
		}
		for (float theta = 0; theta < Math.PI * 2.0; theta += Math.PI / 10.0) {
			for (float ceta = 0; ceta <= Math.PI; ceta += Math.PI / 10.0) {
				GL11.glVertex3f(
						center.x
								+ (float) (Math.cos(theta) * Math.cos(ceta) * radius),
						center.y + (float) (Math.sin(theta) * radius),
						center.z
								+ (float) (Math.cos(theta) * Math.sin(ceta) * radius));
				GL11.glVertex3f(
						center.x
								+ (float) (Math.cos(theta)
										* Math.cos(ceta + (Math.PI / 10.0)) * radius),
						center.y + (float) (Math.sin(theta) * radius),
						center.z
								+ (float) (Math.cos(theta)
										* Math.sin(ceta + (Math.PI / 10.0)) * radius));
				GL11.glVertex3f(
						center.x
								+ (float) (Math.cos(theta + (Math.PI / 10.0))
										* Math.cos(ceta + (Math.PI / 10.0)) * radius),
						center.y
								+ (float) (Math.sin(theta + (Math.PI / 10.0)) * radius),
						center.z
								+ (float) (Math.cos(theta + (Math.PI / 10.0))
										* Math.sin(ceta + (Math.PI / 10.0)) * radius));

				GL11.glVertex3f(
						center.x
								+ (float) (Math.cos(theta + (Math.PI / 10.0))
										* Math.cos(ceta) * radius),
						center.y
								+ (float) (Math.sin(theta + (Math.PI / 10.0)) * radius),
						center.z
								+ (float) (Math.cos(theta + (Math.PI / 10.0))
										* Math.sin(ceta) * radius));
			}
		}
		GL11.glEnd();
	}
}
