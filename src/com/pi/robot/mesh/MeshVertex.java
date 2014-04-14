package com.pi.robot.mesh;

import com.pi.math.Vector3D;

public class MeshVertex {
	Vector3D position;
	Vector3D normal;
	FloatBufferColor color;
	private float[] textureUV;

	public static boolean compareNormals = true;

	public MeshVertex(Vector3D pos, Vector3D normal, float[] tex) {
		this.position = pos;
		this.normal = normal;
		this.textureUV = tex;
	}

	public void setColor(FloatBufferColor color) {
		this.color = color;
	}

	public FloatBufferColor getColor() {
		return color;
	}

	public Vector3D getPosition() {
		return position;
	}

	public Vector3D getNormal() {
		return normal;
	}

	public float[] getTextureUV() {
		return textureUV;
	}

	public int hashCode() {
		return position.hashCode();
	}

	public boolean equals(Object o) {
		if (o instanceof MeshVertex) {
			MeshVertex v = (MeshVertex) o;
			// Are normals similar?
			return v.getPosition().equals(getPosition())
					&& (!compareNormals || Vector3D.dotProduct(v.getNormal(),
							getNormal()) > 0.75)
					&& (v.color == color || (v.color != null && color != null && v.color
							.equals(color)));
		}
		return false;
	}
}
