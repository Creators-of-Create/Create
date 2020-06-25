package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChuteFunnelGenerator extends SpecialBlockStateGen {

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
		boolean pushing = state.get(ChuteFunnelBlock.PUSHING);
		boolean powered = state.get(ChuteFunnelBlock.POWERED);
		if (pushing && !powered)
			return AssetLookup.partialBaseModel(ctx, prov);
		String suffix = (pushing ? "push_" : "pull_") + (powered ? "on" : "off");
		String textureName = "belt_funnel_" + suffix;
		String modelName = ctx.getName() + "_" + suffix;
		return prov.models()
			.withExistingParent(modelName, prov.modLoc("block/" + ctx.getName() + "/block"))
			.texture("3", prov.modLoc("block/" + textureName));
	}

}
