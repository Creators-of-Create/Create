package com.simibubi.create.foundation.render;

import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.SuperBufferFactory;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlwSuperBufferFactory implements SuperBufferFactory {

	@Override
	public SuperByteBuffer create(BufferBuilder builder) {
		return new FlwSuperByteBuffer(builder);
	}

	@Override
	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState, PoseStack ms) {
		return create(ModelUtil.getBufferBuilder(model, referenceState, ms));
	}
}
