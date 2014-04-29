package com.pi.robot.physics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.pi.math.TransMatrix;

public class CompositeArea extends BoundingArea {
	public List<BoundingArea> areas = new ArrayList<BoundingArea>();

	public CompositeArea(BoundingArea... areas) {
		this.areas.addAll(Arrays.asList(areas));
	}

	@Override
	public boolean collidesInternal(BoundingArea b) {
		for (BoundingArea a : areas) {
			if (a.collides(b)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void transform(TransMatrix m) {
		for (BoundingArea a : areas) {
			a.transform(m);
		}
	}

	@Override
	public BoundingArea copy() {
		List<BoundingArea> areas = new ArrayList<BoundingArea>();
		for (BoundingArea a : this.areas) {
			areas.add(a.copy());
		}
		return new CompositeArea(areas.toArray(new BoundingArea[0]));
	}
}
