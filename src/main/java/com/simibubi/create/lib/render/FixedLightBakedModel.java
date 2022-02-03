package com.simibubi.create.lib.render;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class FixedLightBakedModel extends ForwardingBakedModel {
	private static final ThreadLocal<FixedLightBakedModel> THREAD_LOCAL = ThreadLocal.withInitial(FixedLightBakedModel::new);

	protected int light;

	public static BakedModel wrap(BakedModel model, int light) {
		if (!((FabricBakedModel) model).isVanillaAdapter()) {
			FixedLightBakedModel wrapper = THREAD_LOCAL.get();
			wrapper.wrapped = model;
			wrapper.light = light;
			return wrapper;
		}
		return model;
	}

	protected FixedLightBakedModel() {
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(quad -> {
			quad.lightmap(light, light, light, light);
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(quad -> {
			quad.lightmap(light, light, light, light);
			return true;
		});
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();
	}
}

