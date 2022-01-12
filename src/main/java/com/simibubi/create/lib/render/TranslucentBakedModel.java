package com.simibubi.create.lib.render;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class TranslucentBakedModel extends ForwardingBakedModel {
	private static final ThreadLocal<TranslucentBakedModel> THREAD_LOCAL = ThreadLocal.withInitial(TranslucentBakedModel::new);

	protected Supplier<Float> alphaSupplier;

	public static BakedModel wrap(BakedModel model, Supplier<Float> alphaSupplier) {
		TranslucentBakedModel wrapper = THREAD_LOCAL.get();
		wrapper.wrapped = model;
		wrapper.alphaSupplier = alphaSupplier;
		return wrapper;
	}

	protected TranslucentBakedModel() {
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(quad -> {
			int alpha = (int) (alphaSupplier.get() * 255) << 24;
			for (int vertex = 0; vertex < 4; vertex++) {
				int color = quad.spriteColor(vertex, 0);
				quad.spriteColor(vertex, 0, color & 0xFFFFFF | alpha);
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}
}
