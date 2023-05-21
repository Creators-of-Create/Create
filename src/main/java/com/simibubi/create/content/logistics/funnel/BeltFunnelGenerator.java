package com.simibubi.create.content.logistics.funnel;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock.Shape;
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

	public BeltFunnelGenerator(String type) {
		this.type = type;
		this.materialBlockTexture = Create.asResource("block/" + type + "_block");
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
		String prefix = "block/funnel/";
		Shape shape = state.getValue(BeltFunnelBlock.SHAPE);
		String shapeName = shape.getSerializedName();
		boolean powered = state.getOptionalValue(BlockStateProperties.POWERED)
			.orElse(false);
		String poweredSuffix = powered ? "_powered" : "_unpowered";
		String shapeSuffix = shape == Shape.PULLING ? "_pull" : shape == Shape.PUSHING ? "_push" : "_neutral";
		String name = ctx.getName() + "_" + shapeName + poweredSuffix;

		return prov.models()
			.withExistingParent(name, prov.modLoc("block/belt_funnel/block_" + shapeName))
			.texture("particle", materialBlockTexture)
			.texture("block", materialBlockTexture)
			.texture("direction", prov.modLoc(prefix + type + "_funnel" + shapeSuffix))
			.texture("redstone", prov.modLoc(prefix + type + "_funnel" + poweredSuffix))
			.texture("base", prov.modLoc(prefix + type + "_funnel"));
	}

}
