package com.pi.robot.physics;

import com.pi.math.TransMatrix;
import com.pi.math.Vector3D;

public class AABB extends BoundingArea {
	public Vector3D min = new Vector3D();
	public Vector3D max = new Vector3D();
	public TransMatrix reverse = null;

	public AABB() {
		this.max = new Vector3D(Float.MIN_VALUE, Float.MIN_VALUE,
				Float.MIN_VALUE);
		this.min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE,
				Float.MAX_VALUE);
	}

	public AABB(Vector3D min, Vector3D max) {
		this.min = min;
		this.max = max;
		clean();
	}

	public AABB clean() {
		Vector3D mmC = min.clone();
		Vector3D mxC = max.clone();
		min.set(Math.min(mmC.x, mxC.x), Math.min(mmC.y, mxC.y),
				Math.min(mmC.z, mxC.z));
		max.set(Math.max(mmC.x, mxC.x), Math.max(mmC.y, mxC.y),
				Math.max(mmC.z, mxC.z));
		return this;
	}

	public void include(Vector3D v) {
		min.set(Math.min(min.x, v.x), Math.min(min.y, v.y),
				Math.min(min.z, v.z));
		max.set(Math.max(max.x, v.x), Math.max(max.y, v.y),
				Math.max(max.z, v.z));
		clean();
	}

	@Override
	public boolean collidesInternal(BoundingArea b) {
		if (b instanceof AABB) {
			AABB box = ((AABB) b);
			return min.x < box.max.x && min.y < box.max.y && min.z < box.max.z
					&& max.x > box.min.x && max.y > box.min.y
					&& max.z > box.min.z;
		}
		return false;
	}

	@Override
	public void transform(TransMatrix m) {
		Vector3D[] pts = { min.clone(), max.clone() };
		max = new Vector3D(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		min = new Vector3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		for (Vector3D x : pts) {
			for (Vector3D y : pts) {
				for (Vector3D z : pts) {
					include(m.multiply(new Vector3D(x.x, y.y, z.z)));
				}
			}
		}
	}

	@Override
	public BoundingArea copy() {
		AABB bb = new AABB();
		bb.min = min.clone();
		bb.max = max.clone();
		return bb;
	}
}
