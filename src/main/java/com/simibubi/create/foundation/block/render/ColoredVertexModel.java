package com.simibubi.create.foundation.block.render;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class ColoredVertexModel extends ForwardingBakedModel {

	private IBlockVertexColor color;

	public ColoredVertexModel(BakedModel originalModel, IBlockVertexColor color) {
		wrapped = originalModel;
		this.color = color;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(quad -> {
			for (int vertex = 0; vertex < 4; vertex++) {
				float x = quad.x(0);
				float y = quad.y(0);
				float z = quad.z(0);
				int color = this.color.getColor(x + pos.getX(), y + pos.getY(), z + pos.getZ());
				quad.spriteColor(vertex, 0, color);
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

}
