package com.pi.robot.physics;

import com.pi.math.TransMatrix;
import com.pi.math.Vector3D;
import com.pi.robot.Bone;
import com.pi.robot.Skeleton;
import com.pi.robot.robot.RobotStateManager;

public class BallController {
	public Vector3D velocity = new Vector3D();
	private Skeleton mgr;

	public BallController(Skeleton mgrr) {
		this.mgr = mgrr;
	}

	public void stepControl(float deltaT) {
		Bone ball = mgr.getBone(RobotStateManager.BALL_ID);
		if (!ball.attached) {
			velocity.z -= 0.0000001 * deltaT;
			Vector3D goingTo = ball.additional.clone().add(
					velocity.clone().multiply(deltaT));
			Bone field = mgr.getBone(RobotStateManager.FIELD_ID);
			Bone robotBase = mgr.getBone(RobotStateManager.DRIVEBASE_ID);
			BoundingArea ballFuture = ball.boundingBox.copy();
			ballFuture.transform(new TransMatrix().setTranslation(goingTo.x,
					goingTo.y, goingTo.z));
			if (field != null
					&& field.collidesRecursive(ballFuture,
							RobotStateManager.BALL_ID)) {
				velocity.multiply(0);
				goingTo = ball.additional;
			}
			if (robotBase != null
					&& robotBase.collidesRecursive(ballFuture,
							RobotStateManager.BALL_ID)) {
				velocity.multiply(0);
				goingTo = ball.additional;
			}
			ball.additional = goingTo;
		}
	}

	public void launch(Vector3D velocity) {
		this.velocity = velocity.clone();
		Bone pos = mgr.getBone(RobotStateManager.BALL_ID);
		pos.setAttached(false);
		if (pos != null) {
			pos.additional = pos.getLocalToWorld().multiply(pos.getBoneStart());
			pos.additional.x += 100;
			pos.additional.y += 100;
			pos.additional.z += 100;
		}
	}
}
