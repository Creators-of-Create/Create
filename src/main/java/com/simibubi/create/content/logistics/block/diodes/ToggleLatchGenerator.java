package com.simibubi.create.content.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class ToggleLatchGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends Block> Vector<ModelFile> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov) {
		String name = ctx.getName();
		Vector<ModelFile> models = makeVector(4);
		ResourceLocation off = existing("latch_off");
		ResourceLocation on = existing("latch_on");

		models.add(prov.getExistingFile(off));
		models.add(prov.withExistingParent(name + "_off_powered", off)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.getExistingFile(on));
		models.add(prov.withExistingParent(name + "_on_powered", on)
			.texture("top", texture(ctx, "powered_powering")));

		return models;
	}

	@Override
	int getModelIndex(BlockState state) {
		return (state.get(ToggleLatchBlock.POWERING) ? 2 : 0) + (state.get(ToggleLatchBlock.POWERED) ? 1 : 0);
	}

}
