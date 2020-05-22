package com.simibubi.create.modules.logistics.block.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class AdjustableRepeaterGenerator extends AbstractDiodeGenerator {

	@Override
	<T extends Block> Vector<ModelFile> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov) {
		Vector<ModelFile> models = makeVector(4);
		String name = ctx.getName();
		ResourceLocation template = existing(name);

		models.add(prov.getExistingFile(template));
		models.add(prov.withExistingParent(name + "_powered", template)
			.texture("top", texture(ctx, "powered")));
		models.add(prov.withExistingParent(name + "_powering", template)
			.texture("torch", poweredTorch())
			.texture("top", texture(ctx, "powering")));
		models.add(prov.withExistingParent(name + "_powered_powering", template)
			.texture("torch", poweredTorch())
			.texture("top", texture(ctx, "powered_powering")));

		return models;
	}

	@Override
	int getModelIndex(BlockState state) {
		return (state.get(AdjustableRepeaterBlock.POWERING) ? 2 : 0) + (state.get(AdjustableRepeaterBlock.POWERED) ? 1 : 0);
	}

}
