package com.simibubi.create.foundation.render;

import javax.annotation.ParametersAreNonnullByDefault;

import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferedData;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.render.DefaultSuperBufferFactory;
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
		return new DefaultSuperBufferFactory().create(builder);//TODO
	}

	@Override
	public SuperByteBuffer createForBlock(BakedModel model, BlockState referenceState, PoseStack ms) {
		ShadeSeparatedBufferedData data = ModelUtil.getBufferedData(model, referenceState, ms);
		FlwSuperByteBuffer sbb = new FlwSuperByteBuffer(data);
		data.release();
		return sbb;
	}
}
