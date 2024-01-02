package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

public class BakedModelRenderHelper {

	public static SuperByteBuffer standardBlockRender(BlockState renderedState) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance()
			.getBlockRenderer();
		return standardModelRender(dispatcher.getBlockModel(renderedState), renderedState);
	}

	public static SuperByteBuffer standardModelRender(BakedModel model, BlockState referenceState) {
		return standardModelRender(model, referenceState, new PoseStack());
	}

	public static SuperByteBuffer standardModelRender(BakedModel model, BlockState referenceState, PoseStack ms) {
		ShadeSeparatedBufferedData data = ModelUtil.getBufferedData(model, referenceState, ms);
		SuperByteBuffer sbb = new SuperByteBuffer(data);
		data.release();
		return sbb;
	}

}
