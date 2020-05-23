package com.simibubi.create.content.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class PulseRepeaterGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends Block> Vector<ModelFile> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov) {
		Vector<ModelFile> models = makeVector(3);
		String name = ctx.getName();
		ResourceLocation template = existing(name);

		models.add(prov.getExistingFile(template));
		models.add(prov.withExistingParent(name + "_powered", template)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.withExistingParent(name + "_pulsing", template)
			.texture("top", texture(ctx, "powered"))
			.texture("torch", poweredTorch()));

		return models;
	}

	@Override
	int getModelIndex(BlockState state) {
		return state.get(PulseRepeaterBlock.PULSING) ? 2 : state.get(PulseRepeaterBlock.POWERED) ? 1 : 0;
	}

}
