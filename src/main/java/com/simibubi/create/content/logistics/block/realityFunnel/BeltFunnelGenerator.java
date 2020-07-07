package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltFunnelGenerator extends SpecialBlockStateGen {

	private String type;

	public BeltFunnelGenerator(String type) {
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
		boolean pushing = state.get(BeltFunnelBlock.PUSHING);
		boolean powered = state.has(BeltFunnelBlock.POWERED) && state.get(BeltFunnelBlock.POWERED);
		String shapeName = state.get(BeltFunnelBlock.SHAPE)
			.getName();
		String suffix = (pushing ? "push" : "pull") + (powered ? "_powered" : "");
		String name = ctx.getName() + "_" + suffix;
		String textureName = type + "_funnel_" + suffix;
		return prov.models()
			.withExistingParent(name + "_" + shapeName, prov.modLoc("block/belt_funnel/block_" + shapeName))
			.texture("particle", prov.modLoc("block/" + type + "_casing"))
			.texture("2", prov.modLoc("block/" + textureName))
			.texture("3", prov.modLoc("block/" + type + "_funnel_back"))
			.texture("4", prov.modLoc("block/" + type + "_funnel_plating"));
	}

}
