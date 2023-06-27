package com.simibubi.create.content.redstone.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class BrassDiodeGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> Vector<ModelFile> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov) {
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
	protected int getModelIndex(BlockState state) {
		return (state.getValue(BrassDiodeBlock.POWERING) ^ state.getValue(BrassDiodeBlock.INVERTED) ? 2 : 0)
			+ (state.getValue(BrassDiodeBlock.POWERED) ? 1 : 0);
	}

}
