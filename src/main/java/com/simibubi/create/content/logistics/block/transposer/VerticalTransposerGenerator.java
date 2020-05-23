package com.simibubi.create.content.logistics.block.transposer;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class VerticalTransposerGenerator extends SpecialBlockStateGen {

	private boolean linked;

	public VerticalTransposerGenerator(boolean linked) {
		this.linked = linked;
	}

	@Override
	protected int getXRotation(BlockState state) {
		return state.get(TransposerBlock.Vertical.UPWARD) ? 270 : 90;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return (state.get(TransposerBlock.Vertical.UPWARD) ? 180 : 0)
			+ horizontalAngle(state.get(TransposerBlock.Vertical.HORIZONTAL_FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return AssetLookup.forPowered(ctx, prov, "transposer/" + (linked ? "vertical_linked" : "block"))
			.apply(state);
	}

}
