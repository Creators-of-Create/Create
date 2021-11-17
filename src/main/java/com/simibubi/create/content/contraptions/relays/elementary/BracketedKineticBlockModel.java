package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.Random;
import java.util.function.Supplier;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.lib.render.VirtualRenderingStateManager;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

public class BracketedKineticBlockModel extends ForwardingBakedModel {

//	private static final ModelProperty<BracketedModelData> BRACKET_PROPERTY = new ModelProperty<>();

	public BracketedKineticBlockModel(BakedModel template) {
		wrapped = template;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		if (VirtualRenderingStateManager.getVirtualState()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		BracketedModelData data = new BracketedModelData();
		BracketedTileEntityBehaviour attachmentBehaviour =
				TileEntityBehaviour.get(blockView, pos, BracketedTileEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());

		BakedModel bracket = data.getBracket();
		if (bracket == null)
			return;
		context.fallbackConsumer().accept(bracket);
	}

	private class BracketedModelData {
		BakedModel bracket;

		public void putBracket(BlockState state) {
			this.bracket = Minecraft.getInstance()
				.getBlockRenderer()
				.getBlockModel(state);
		}

		public BakedModel getBracket() {
			return bracket;
		}

	}

}
