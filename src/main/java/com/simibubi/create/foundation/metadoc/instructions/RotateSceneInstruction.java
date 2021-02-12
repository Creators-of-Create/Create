package com.simibubi.create.foundation.metadoc.instructions;

import com.simibubi.create.foundation.metadoc.MetaDocInstruction;
import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.MetaDocScene.SceneTransform;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;

public class RotateSceneInstruction extends MetaDocInstruction {

	private float xRot;
	private float yRot;
	private boolean relative;

	public RotateSceneInstruction(float xRot, float yRot, boolean relative) {
		this.xRot = xRot;
		this.yRot = yRot;
		this.relative = relative;
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public void tick(MetaDocScene scene) {
		SceneTransform transform = scene.getTransform();
		float targetX = relative ? transform.xRotation.getChaseTarget() + xRot : xRot;
		float targetY = relative ? transform.yRotation.getChaseTarget() + yRot : yRot;
		transform.xRotation.chase(targetX, .1f, Chaser.EXP);
		transform.yRotation.chase(targetY, .1f, Chaser.EXP);
	}

}
