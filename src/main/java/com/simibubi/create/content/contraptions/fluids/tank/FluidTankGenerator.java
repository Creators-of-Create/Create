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

	private String prefix;

	public FluidTankGenerator() {
		this("");
	}

	public FluidTankGenerator(String prefix) {
		this.prefix = prefix;
	}

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

		String modelName = shapeName + (shape == Shape.PLAIN ? "" : "_" + shape.name());

		if (!prefix.isEmpty())
			return prov.models()
				.withExistingParent(prefix + modelName, prov.modLoc("block/fluid_tank/block_" + modelName))
				.texture("0", prov.modLoc("block/" + prefix + "casing"))
				.texture("1", prov.modLoc("block/" + prefix + "fluid_tank"))
				.texture("3", prov.modLoc("block/" + prefix + "fluid_tank_window"))
				.texture("4", prov.modLoc("block/" + prefix + "fluid_tank_window_single"))
				.texture("particle", prov.modLoc("block/" + prefix + "fluid_tank"));

		return AssetLookup.partialBaseModel(ctx, prov, modelName);
	}

}
