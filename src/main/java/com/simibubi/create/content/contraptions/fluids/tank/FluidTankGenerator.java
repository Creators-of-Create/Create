package com.simibubi.create.content.contraptions.fluids.tank;

import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class FluidTankGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return 0;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		Boolean top = state.get(FluidTankBlock.TOP);
		Boolean bottom = state.get(FluidTankBlock.BOTTOM);
		Shape shape = state.get(FluidTankBlock.SHAPE);

		String shapeName = "middle";
		if (top && bottom)
			shapeName = "single";
		else if (top)
			shapeName = "top";
		else if (bottom)
			shapeName = "bottom";

		return AssetLookup.partialBaseModel(ctx, prov,
			shapeName + (shape == Shape.PLAIN ? "" : "_" + shape.getString()));
	}

}
