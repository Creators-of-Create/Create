package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.foundation.utility.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class RedstoneLinkGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		Direction facing = state.get(RedstoneLinkBlock.FACING);
		return facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 270;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Direction facing = state.get(RedstoneLinkBlock.FACING);
		return facing.getAxis()
			.isVertical() ? 180 : horizontalAngle(facing);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		String variant = state.get(RedstoneLinkBlock.RECEIVER) ? "receiver" : "transmitter";
		if (state.get(RedstoneLinkBlock.FACING).getAxis().isHorizontal())
			variant += "_vertical";
		if (state.get(RedstoneLinkBlock.POWERED))
			variant += "_powered";
		
		return prov.models().getExistingFile(prov.modLoc("block/redstone_link/" + variant));
	}

}
