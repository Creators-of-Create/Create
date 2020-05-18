package com.simibubi.create.foundation.utility.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class SpecialBlockStateGen {

	public final <T extends Block> void generate(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStates(state -> {
				return ConfiguredModel.builder()
					.modelFile(getModel(ctx, prov, state))
					.rotationX(getXRotation(state))
					.rotationY(getYRotation(state))
					.build();
			});
	}

	protected abstract int getXRotation(BlockState state);

	protected abstract int getYRotation(BlockState state);

	public abstract <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, BlockState state);

}
