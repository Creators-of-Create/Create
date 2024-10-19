package com.simibubi.create.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;

public interface BogeyVisual {
	void update(float wheelAngle, PoseStack poseStack);

	void hide();

	void updateLight(int packedLight);

	void delete();
}
