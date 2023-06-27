package com.simibubi.create.content.redstone.diodes;

import java.util.Vector;

import com.tterrag.registrate.providers.DataGenContext;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

public class PoweredLatchGenerator extends AbstractDiodeGenerator {

	@Override
	protected <T extends Block> Vector<ModelFile> createModels(DataGenContext<Block, T> ctx, BlockModelProvider prov) {
		Vector<ModelFile> models = makeVector(2);
		String name = ctx.getName();
		ResourceLocation off = existing("latch_off");
		ResourceLocation on = existing("latch_on");

		models.add(prov.withExistingParent(name, off)
			.texture("top", texture(ctx, "idle")));
		models.add(prov.withExistingParent(name + "_powered", on)
			.texture("top", texture(ctx, "powering")));

		return models;
	}

	@Override
	protected int getModelIndex(BlockState state) {
		return state.getValue(PoweredLatchBlock.POWERING) ? 1 : 0;
	}

}
