package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.TessellatedModel;
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

	public static SuperByteBuffer standardModelRender(BakedModel bakedModel, BlockState referenceState, PoseStack ms) {
		var model = new BakedModelBuilder(bakedModel).blockState(referenceState)
				.poseStack(ms)
				.disableShadeSeparation()
				.build();

		SuperByteBuffer out = null;
		for (Mesh value : model.meshes()
				.values()) {
			out = new SuperByteBuffer(value);
			break;
		}
		model.delete();
		return out;
	}

}
