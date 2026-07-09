package com.simibubi.create.content.logistics.funnel;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.tterrag.registrate.providers.generators.ModelFile;

import java.util.HashMap;
import java.util.Map;

public class BeltFunnelGenerator extends SpecialBlockStateGen {

	private String type;
	private Identifier materialBlockTexture;
	private final Map<String, ModelFile> models = new HashMap<>();

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
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
		BlockState state) {
		String prefix = "block/funnel/";
		Shape shape = state.getValue(BeltFunnelBlock.SHAPE);
		String shapeName = shape.getSerializedName();
		boolean powered = state.getOptionalValue(BlockStateProperties.POWERED)
			.orElse(false);
		String poweredSuffix = powered ? "_powered" : "_unpowered";
		String shapeSuffix = shape == Shape.PULLING ? "_pull" : shape == Shape.PUSHING ? "_push" : "_neutral";
		String name = ctx.getName() + "_" + shapeName + poweredSuffix;

		return models.computeIfAbsent(name, $ -> prov.models()
			.withExistingParent(name, prov.modLoc("block/belt_funnel/block_" + shapeName))
			.texture("particle", materialBlockTexture)
			.texture("block", materialBlockTexture)
			.texture("direction", prov.modLoc(prefix + type + "_funnel" + shapeSuffix))
			.texture("redstone", prov.modLoc(prefix + type + "_funnel" + poweredSuffix))
			.texture("base", prov.modLoc(prefix + type + "_funnel")));
	}

}
