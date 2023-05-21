package com.simibubi.create.content.redstone.link;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class RedstoneLinkGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		Direction facing = state.getValue(RedstoneLinkBlock.FACING);
		return facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 270;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Direction facing = state.getValue(RedstoneLinkBlock.FACING);
		return facing.getAxis()
			.isVertical() ? 180 : horizontalAngle(facing);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		String variant = state.getValue(RedstoneLinkBlock.RECEIVER) ? "receiver" : "transmitter";
		if (state.getValue(RedstoneLinkBlock.FACING).getAxis().isHorizontal())
			variant += "_vertical";
		if (state.getValue(RedstoneLinkBlock.POWERED))
			variant += "_powered";
		
		return prov.models().getExistingFile(prov.modLoc("block/redstone_link/" + variant));
	}

}
