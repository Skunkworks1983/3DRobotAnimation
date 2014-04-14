package com.pi.robot.robot;

public class Changeable<E> {
	E state;
	E lastProcessed;

	public Changeable(E initial) {
		this.state = initial;
	}

	public void setState(E state) {
		this.state = state;
	}

	public E getState() {
		return state;
	}

	public boolean hasChanged() {
		if (state != lastProcessed) {
			lastProcessed = state;
			return true;
		}
		return false;
	}
}
