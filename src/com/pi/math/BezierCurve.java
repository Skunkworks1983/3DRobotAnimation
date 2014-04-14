package com.pi.math;

import java.util.Arrays;

public class BezierCurve {
	private Vector3D[] curve;
	private double[] angles;
	private double[] distances;
	private Vector3D[] controlPoints;

	public BezierCurve(Vector3D[] controlPoints, double resolution) {
		this.controlPoints = controlPoints;
		genCurve(resolution);
	}

	public Vector3D[] getPoints() {
		return curve;
	}

	public double[] getDistances() {
		return distances;
	}

	public double[] getAngles() {
		return angles;
	}

	public Vector3D[] getControlPoints() {
		return controlPoints;
	}

	private void genCurve(double resolution) {
		int size = (int) (1D / resolution);
		curve = new Vector3D[size];
		angles = new double[size];
		distances = new double[size];

		int i = 0;
		Vector3D[] quad1Control = Arrays.copyOfRange(controlPoints, 0, 3);
		Vector3D[] quad2Control = Arrays.copyOfRange(controlPoints, 1, 4);
		for (double t = 0; t < 1 && i < curve.length; t += resolution, i++) {
			curve[i] = getCurvePoint(t, controlPoints);
			if (i > 0) {
				distances[i] = curve[i - 1].dist(curve[i]);
			}
			Vector3D a1 = getCurvePoint(t, quad1Control);
			Vector3D a2 = getCurvePoint(t, quad2Control);
			angles[i] = Math.PI + Math.atan2(a2.y - a1.y, a2.x - a1.x);
		}
	}

	private Vector3D getCurvePoint(double i, Vector3D... controlPoints) {
		if (controlPoints.length != 3 && controlPoints.length != 4) {
			throw new IllegalArgumentException(
					"Only quadratic and cubic curves!");
		}
		Vector3D result = controlPoints[0].clone().multiply(
				(float) ((1 - i) * (1 - i) * (controlPoints.length == 4 ? 1 - i
						: 1)));
		if (controlPoints.length == 4) {
			result.add(controlPoints[1].clone().multiply(
					(float) (i * (1 - i) * (1 - i) * 3)));
			result.add(controlPoints[2].clone().multiply(
					(float) (i * i * (1 - i) * 3)));
		} else {
			result.add(controlPoints[1].clone().multiply(
					(float) (2D * (1 - i) * i)));
		}
		result.add(controlPoints[controlPoints.length - 1].clone().multiply(
				(float) (i * i * (controlPoints.length == 4 ? i : 1))));
		return result;
	}
}
