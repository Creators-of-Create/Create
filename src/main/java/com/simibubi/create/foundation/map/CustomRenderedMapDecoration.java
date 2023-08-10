package com.simibubi.create.foundation.map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public interface CustomRenderedMapDecoration {
	void render(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight, MapItemSavedData mapData, int index);
}
