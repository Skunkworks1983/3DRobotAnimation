package com.pi.robot;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.pi.robot.physics.BoundingArea;

import com.pi.math.Quaternion;
import com.pi.math.TransMatrix;
import com.pi.math.Vector3D;
import com.pi.robot.mesh.Mesh;
import com.pi.robot.physics.AABB;
import com.pi.robot.robot.NotificationBubble;
import com.pi.robot.robot.RobotStateManager;

public class Bone {
	// Parameters
	private Bone parent;
	private List<Bone> children = new ArrayList<Bone>();
	private Quaternion baseRotation = new Quaternion();
	private Quaternion bonusRotation = new Quaternion();
	private float length;
	public Vector3D base;
	public boolean visible = true;

	// Calculated
	private Quaternion calcRotation = new Quaternion();
	private Vector3D boneStart = new Vector3D();
	private Vector3D boneEnd = new Vector3D();
	private TransMatrix localToWorld = new TransMatrix();
	public Mesh mesh;
	public Map<Vector3D, NotificationBubble> notifications = new HashMap<Vector3D, NotificationBubble>();

	public Vector3D additional = new Vector3D();
	public BoundingArea baseBoundingBox;
	public BoundingArea boundingBox = new AABB();

	private int id;

	public Bone(int id, Vector3D pos) {
		this(id, new Vector3D(), pos);
	}

	public Bone(int id, Vector3D base, Vector3D pos) {
		this.base = base;
		this.id = id;

		Vector3D u = new Vector3D(0, 0, 1);
		Vector3D v = pos.subtract(base).clone().normalize();

		this.length = pos.magnitude();

		if (u.equals(Vector3D.negative(v))) {
			baseRotation.setRaw(0, 0, 1, 0);
		} else {
			Vector3D half = u.clone().add(v).normalize();
			baseRotation.setRaw(Vector3D.dotProduct(u, half),
					Vector3D.crossProduct(u, half));
		}
	}

	public Bone(int id, Bone parent, Vector3D base, Vector3D pos) {
		this(id, parent.getLocalToWorld().inverse().multiply(base)
				.subtract(new Vector3D(0, 0, parent.length)), parent
				.getLocalToWorld().inverse().multiply(pos)
				.subtract(new Vector3D(0, 0, parent.length)));
		// System.out.println(parent.getLocalToWorld().inverse().multiply(base));
		this.parent = parent;
		this.parent.children.add(this);
		calculate();
	}

	public void addChild(Bone b) {
		if (b.parent != this && b.parent != null) {
			throw new RuntimeException("This child is already claimed.");
		}
		if (!children.contains(b)) {
			children.add(b);
		}
		b.parent = this;
	}

	public void calculate() {
		calcRotation = baseRotation.clone().multiply(bonusRotation);
		if (parent != null && attached) {
			Vector3D parentOffset = new Vector3D(0, 0, parent.length).add(base);
			boneStart.set(parent.localToWorld.multiply(parentOffset));
			// Parent matrix * local matrix
			TransMatrix me = new TransMatrix(calcRotation, parentOffset.x,
					parentOffset.y, parentOffset.z);
			localToWorld.copy(parent.localToWorld).multiply(me);
		} else {
			boneStart.set(base);
			localToWorld.setQuaternion(calcRotation, boneStart.x, boneStart.y,
					boneStart.z);
		}
		boneEnd = localToWorld.multiply(new Vector3D(0, 0, length));
		if (baseBoundingBox == null && mesh != null) {
			baseBoundingBox = mesh.boundingBox.copy();
		}
		if (baseBoundingBox != null) {
			boundingBox = baseBoundingBox.copy();
			if (id != RobotStateManager.FIELD_ID)
				boundingBox.transform(localToWorld);
		}
	}

	public void calculateRecursive() {
		calculate();
		for (Bone b : children) {
			b.calculateRecursive();
		}
	}

	public List<Bone> getChildren() {
		return children;
	}

	public Vector3D getBoneStart() {
		return boneStart;
	}

	public Vector3D getBoneEnd() {
		return boneEnd;
	}

	public TransMatrix getLocalToWorld() {
		return localToWorld;
	}

	public void slerp(Quaternion from, Quaternion to, float time) {
		bonusRotation.slerp(from, to, time);
	}

	public void setYPR(float yaw, float pitch, float roll) {
		bonusRotation.setRotation(roll, pitch, yaw);
	}

	boolean vboCreated = false;
	public boolean attached = true;

	public void draw() {
		if (!visible) {
			return;
		}
		if (mesh == null) {
			return;
		}
		if (/* mesh.getIndicies() != null && */!vboCreated) {
			mesh.loadToGPU();
			vboCreated = true;
		}
		FloatBuffer buffer = RobotStateManager.defaultColor.getBuffer();
		GL11.glColor4f(buffer.get(), buffer.get(), buffer.get(), buffer.get());
		GL11.glPushMatrix();
		GL11.glMultMatrix(localToWorld.toBuffer(BufferUtils
				.createFloatBuffer(16)));
		GL11.glTranslatef(additional.x, additional.y, additional.z);
		mesh.draw();
		for (NotificationBubble bb : notifications.values()) {
			bb.render();
		}
		GL11.glPopMatrix();
	}

	public void setAttached(boolean attached) {
		this.attached = attached;
		calculate();
	}

	public boolean collidesRecursive(BoundingArea b, int... ignore) {
		if (boundingBox != null) {
			for (int j : ignore) {
				if (j == id) {
					return false;
				}
			}
			if (boundingBox.collides(b)) {
				return true;
			}
		}
		for (Bone bone : children) {
			if (bone.attached && bone.collidesRecursive(b, ignore)) {
				return true;
			}
		}
		return false;
	}
}
