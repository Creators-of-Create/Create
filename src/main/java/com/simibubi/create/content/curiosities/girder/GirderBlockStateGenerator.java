package com.simibubi.create.content.curiosities.girder;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.utility.Iterate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

public class GirderBlockStateGenerator {

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
