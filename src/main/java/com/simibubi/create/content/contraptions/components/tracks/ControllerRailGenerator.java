package com.simibubi.create.content.contraptions.components.tracks;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraftforge.client.model.generators.ModelFile;

public class ControllerRailGenerator extends SpecialBlockStateGen {

	@Override
	protected Property<?>[] getIgnoredProperties() {
		return new Property<?>[] { ControllerRailBlock.POWER };
	}

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		RailShape shape = state.getValue(ControllerRailBlock.SHAPE);
		boolean backwards = ControllerRailBlock.isStateBackwards(state);
		int rotation = backwards ? 180 : 0;

		return switch (shape) {
			case EAST_WEST, ASCENDING_WEST -> rotation + 270;
			case ASCENDING_EAST -> rotation + 90;
			case ASCENDING_SOUTH -> rotation + 180;
			default -> rotation;
		};
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		RailShape shape = state.getValue(ControllerRailBlock.SHAPE);
		boolean backwards = ControllerRailBlock.isStateBackwards(state);

		String model = shape.isAscending() ? backwards ? "ascending_south" : "ascending_north" : "north_south";
		return AssetLookup.partialBaseModel(ctx, prov, model);
	}

}
