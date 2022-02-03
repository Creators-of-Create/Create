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

public class FixedColorTintingBakedModel extends ForwardingBakedModel {
	private static final ThreadLocal<FixedColorTintingBakedModel> THREAD_LOCAL = ThreadLocal.withInitial(FixedColorTintingBakedModel::new);

	protected int color;

	public static BakedModel wrap(BakedModel model, int color) {
		if (!((FabricBakedModel) model).isVanillaAdapter()) {
			FixedColorTintingBakedModel wrapper = THREAD_LOCAL.get();
			wrapper.wrapped = model;
			wrapper.color = color;
			return wrapper;
		}
		return model;
	}

	protected FixedColorTintingBakedModel() {
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(quad -> {
			if (quad.colorIndex() != -1) {
				quad.spriteColor(0, color, color, color, color);
				quad.colorIndex(-1);
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.pushTransform(quad -> {
			if (quad.colorIndex() != -1) {
				quad.spriteColor(0, color, color, color, color);
				quad.colorIndex(-1);
			}
			return true;
		});
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();
	}
}

