package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChuteFunnelGenerator extends SpecialBlockStateGen {

	private String type;

	public ChuteFunnelGenerator(String type) {
		this.type = type;
	}

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
		boolean powered = state.has(ChuteFunnelBlock.POWERED) && state.get(ChuteFunnelBlock.POWERED);
		String suffix = (pushing ? "push" : "pull") + (powered ? "_powered" : "");
		String textureName = type + "_funnel_" + suffix;
		String modelName = ctx.getName() + "_" + suffix;
		return prov.models()
			.withExistingParent(modelName, prov.modLoc("block/chute_funnel/block"))
			.texture("particle", prov.modLoc("block/" + type + "_casing"))
			.texture("3", prov.modLoc("block/" + textureName))
			.texture("1_2", prov.modLoc("block/" + type + "_funnel_back"))
			.texture("4", prov.modLoc("block/" + type + "_funnel_plating"));
	}

}
