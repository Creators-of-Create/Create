package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltFunnelGenerator extends SpecialBlockStateGen {

	private String type;
	private ResourceLocation materialBlockTexture;

	public BeltFunnelGenerator(String type, ResourceLocation materialBlockTexture) {
		this.type = type;
		this.materialBlockTexture = materialBlockTexture;
	}

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(BeltFunnelBlock.HORIZONTAL_FACING)) + 180;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		boolean powered = state.getOptionalValue(BlockStateProperties.POWERED).orElse(false);
		String shapeName = state.getValue(BeltFunnelBlock.SHAPE)
			.getSerializedName();
		
		String poweredSuffix = powered ? "_powered" : "";
		String name = ctx.getName() + "_" + poweredSuffix;
		
		return prov.models()
			.withExistingParent(name + "_" + shapeName, prov.modLoc("block/belt_funnel/block_" + shapeName))
			.texture("particle", materialBlockTexture)
			.texture("2", prov.modLoc("block/" + type + "_funnel_neutral"))
			.texture("2_1", prov.modLoc("block/" + type + "_funnel_push"))
			.texture("2_2", prov.modLoc("block/" + type + "_funnel_pull"))
			.texture("3", prov.modLoc("block/" + type + "_funnel_back"))
			.texture("5", prov.modLoc("block/" + type + "_funnel_tall" + poweredSuffix))
			.texture("6", prov.modLoc("block/" + type + "_funnel" + poweredSuffix))
			.texture("7", prov.modLoc("block/" + type + "_funnel_plating"));
	}

}
