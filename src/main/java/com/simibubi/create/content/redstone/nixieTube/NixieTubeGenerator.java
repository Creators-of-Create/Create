package com.simibubi.create.content.redstone.nixieTube;

import com.simibubi.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class NixieTubeGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.getValue(NixieTubeBlock.FACE)
			.xRot();
	}

	@Override
	protected int getYRotation(BlockState state) {
		DoubleAttachFace face = state.getValue(NixieTubeBlock.FACE);
		return horizontalAngle(state.getValue(NixieTubeBlock.FACING))
			+ (face == DoubleAttachFace.WALL || face == DoubleAttachFace.WALL_REVERSED ? 180 : 0);
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return prov.models()
			.withExistingParent(ctx.getName(), prov.modLoc("block/nixie_tube/block"));
	}

}
