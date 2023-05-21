package com.simibubi.create.content.decoration.girder;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.utility.Iterate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

public class GirderBlockStateGenerator {

	public static void blockStateWithShaft(DataGenContext<Block, GirderEncasedShaftBlock> c,
		RegistrateBlockstateProvider p) {
		MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p))
			.rotationY(0)
			.addModel()
			.condition(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Axis.Z)
			.end();

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p))
			.rotationY(90)
			.addModel()
			.condition(GirderEncasedShaftBlock.HORIZONTAL_AXIS, Axis.X)
			.end();

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p, "top"))
			.addModel()
			.condition(GirderEncasedShaftBlock.TOP, true)
			.end();

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p, "bottom"))
			.addModel()
			.condition(GirderEncasedShaftBlock.BOTTOM, true)
			.end();

	}

	public static void blockState(DataGenContext<Block, GirderBlock> c, RegistrateBlockstateProvider p) {
		MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p, "pole"))
			.addModel()
			.condition(GirderBlock.X, false)
			.condition(GirderBlock.Z, false)
			.end();

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p, "x"))
			.addModel()
			.condition(GirderBlock.X, true)
			.end();

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p, "z"))
			.addModel()
			.condition(GirderBlock.Z, true)
			.end();

		for (boolean x : Iterate.trueAndFalse)
			builder.part()
				.modelFile(AssetLookup.partialBaseModel(c, p, "top"))
				.addModel()
				.condition(GirderBlock.TOP, true)
				.condition(GirderBlock.X, x)
				.condition(GirderBlock.Z, !x)
				.end()
				.part()
				.modelFile(AssetLookup.partialBaseModel(c, p, "bottom"))
				.addModel()
				.condition(GirderBlock.BOTTOM, true)
				.condition(GirderBlock.X, x)
				.condition(GirderBlock.Z, !x)
				.end();

		builder.part()
			.modelFile(AssetLookup.partialBaseModel(c, p, "cross"))
			.addModel()
			.condition(GirderBlock.X, true)
			.condition(GirderBlock.Z, true)
			.end();

	}

}
