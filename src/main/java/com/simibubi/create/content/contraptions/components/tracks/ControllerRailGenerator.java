package com.simibubi.create.content.contraptions.components.tracks;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.properties.RailShape;
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
		RailShape shape = state.get(ControllerRailBlock.SHAPE);
		boolean backwards = ControllerRailBlock.isStateBackwards(state);
		int rotation = backwards ? 180 : 0;

		switch (shape) {
		case EAST_WEST:
		case ASCENDING_WEST:
			return rotation + 270;
		case ASCENDING_EAST:
			return rotation + 90;
		case ASCENDING_SOUTH:
			return rotation + 180;
		default:
			return rotation;
		}
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		RailShape shape = state.get(ControllerRailBlock.SHAPE);
		boolean backwards = ControllerRailBlock.isStateBackwards(state);

		String model = shape.isAscending() ? backwards ? "ascending_south" : "ascending_north" : "north_south";
		return AssetLookup.partialBaseModel(ctx, prov, model);
	}

}
