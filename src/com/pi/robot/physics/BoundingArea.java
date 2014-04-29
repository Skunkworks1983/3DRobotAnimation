package com.pi.robot.physics;

import java.util.List;

import com.pi.math.TransMatrix;

public abstract class BoundingArea {

	protected abstract boolean collidesInternal(BoundingArea b);

	public final boolean collides(BoundingArea b) {
		if (b instanceof CompositeArea) {
			List<BoundingArea> subs = ((CompositeArea) b).areas;
			for (BoundingArea a : subs) {
				if (collides(a)) {
					return true;
				}
			}
			return false;
		}
		return collidesInternal(b);
	}

	public abstract void transform(TransMatrix m);
	
	public abstract BoundingArea copy();
}
