package com.simibubi.create.content.logistics.packagerLink;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import net.neoforged.neoforge.client.model.generators.ModelFile;

public class PackagerLinkGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(PackagerLinkBlock.FACE) == AttachFace.CEILING ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Direction facing = state.getValue(PackagerLinkBlock.FACING);
		return horizontalAngle(facing);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
												BlockState state) {
		String variant =
			state.getValue(PackagerLinkBlock.FACE) == AttachFace.WALL ? "block_horizontal" : "block_vertical";
		if (state.getValue(PackagerLinkBlock.POWERED))
			variant += "_powered";
		return prov.models()
			.getExistingFile(prov.modLoc("block/stock_link/" + variant));
	}

}
