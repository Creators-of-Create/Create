package com.simibubi.create.foundation.render;

import java.util.Random;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.lib.render.VirtualRenderingStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
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
		BufferBuilder builder = getBufferBuilder(model, referenceState, ms);

		return new SuperByteBuffer(builder);
	}

	public static BufferBuilder getBufferBuilder(BakedModel model, BlockState referenceState, PoseStack ms) {
		Minecraft mc = Minecraft.getInstance();
		BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
		ModelBlockRenderer blockRenderer = dispatcher.getModelRenderer();
		BufferBuilder builder = new BufferBuilder(512);

		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		VirtualRenderingStateManager.runVirtually(() -> blockRenderer.tesselateBlock(mc.level, model, referenceState, BlockPos.ZERO.above(255), ms, builder, true,
			new Random(), 42, OverlayTexture.NO_OVERLAY));
		builder.end();
		return builder;
	}

}
