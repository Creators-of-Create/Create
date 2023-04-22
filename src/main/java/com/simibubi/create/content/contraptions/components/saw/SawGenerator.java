package com.simibubi.create.content.contraptions.components.saw;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class SawGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(SawBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Direction facing = state.getValue(SawBlock.FACING);
		boolean axisAlongFirst = state.getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
		if (facing.getAxis()
			.isVertical())
			return (axisAlongFirst ? 270 : 0) + (state.getValue(SawBlock.FLIPPED) ? 180 : 0);
		return horizontalAngle(facing);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		String path = "block/" + ctx.getName() + "/";
		String orientation = state.getValue(SawBlock.FACING)
			.getAxis()
			.isVertical() ? "vertical" : "horizontal";

		return prov.models()
			.getExistingFile(prov.modLoc(path + orientation));
	}

}
