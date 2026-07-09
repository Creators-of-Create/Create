package com.simibubi.create.foundation.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import com.tterrag.registrate.providers.generators.ConfiguredModel;
import com.tterrag.registrate.providers.generators.ModelFile;

public abstract class SpecialBlockStateGen {

	protected Property<?>[] getIgnoredProperties() {
		return new Property<?>[0];
	}

	public final <T extends Block> void generate(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov) {
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
		if (direction.getAxis()
			.isVertical())
			return 0;
		return (int) direction.toYRot();
	}

	protected abstract int getXRotation(BlockState state);

	protected abstract int getYRotation(BlockState state);

	public abstract <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx,
		RegistrateBlockModelGenerator prov, BlockState state);

}
