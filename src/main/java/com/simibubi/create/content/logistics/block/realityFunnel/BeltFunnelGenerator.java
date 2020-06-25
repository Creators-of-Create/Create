package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltFunnelGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.get(BeltFunnelBlock.HORIZONTAL_FACING)) + 180;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		boolean pushing = state.get(BeltFunnelBlock.PUSHING);
		boolean powered = state.get(BeltFunnelBlock.POWERED);
		String shapeName = state.get(BeltFunnelBlock.SHAPE)
			.getName();
		if (pushing && !powered)
			return AssetLookup.partialBaseModel(ctx, prov, shapeName);
		String name = ctx.getName() + "_" + (pushing ? "push_" : "pull_") + (powered ? "on" : "off");
		return prov.models()
			.withExistingParent(name + "_" + shapeName, prov.modLoc("block/" + ctx.getName() + "/block_" + shapeName))
			.texture("2", prov.modLoc("block/" + name));
	}

}
