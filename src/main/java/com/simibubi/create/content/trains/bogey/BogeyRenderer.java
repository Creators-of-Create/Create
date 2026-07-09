package com.simibubi.create.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;

public interface BogeyRenderer {
	void render(CompoundTag bogeyData, float wheelAngle, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean inContraption);
}
