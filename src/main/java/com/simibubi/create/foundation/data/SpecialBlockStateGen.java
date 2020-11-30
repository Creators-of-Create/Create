package com.simibubi.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class SpecialBlockStateGen {

	protected IProperty<?>[] getIgnoredProperties() {
		return new IProperty<?>[0];
	}
	
	public final <T extends Block> void generate(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {
		prov.getVariantBuilder(ctx.getEntry())
			.forAllStatesExcept(state -> {
				return ConfiguredModel.builder()
					.modelFile(getModel(ctx, prov, state))
					.rotationX((getXRotation(state) + 360) % 360)
					.rotationY((getYRotation(state) + 360) % 360)
					.build();
			}, getIgnoredProperties());
	}

	protected int horizontalAngle(Direction direction) {
		if (direction.getAxis().isVertical())
			return 0;
		return (int) direction.getHorizontalAngle();
	}
	
	protected abstract int getXRotation(BlockState state);

	protected abstract int getYRotation(BlockState state);

	public abstract <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, BlockState state);

}
