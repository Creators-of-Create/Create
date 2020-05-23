package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class VerticalFunnelGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.get(FunnelBlock.UPWARD) ? 270 : 90;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return (state.get(FunnelBlock.UPWARD) ? 180 : 0) + horizontalAngle(state.get(FunnelBlock.HORIZONTAL_FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return prov.models()
			.getExistingFile(prov.modLoc("block/funnel/vertical"));
	}

}
