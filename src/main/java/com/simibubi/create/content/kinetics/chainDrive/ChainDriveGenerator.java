package com.simibubi.create.content.kinetics.chainDrive;

import java.util.function.BiFunction;

import com.simibubi.create.content.kinetics.chainDrive.ChainDriveBlock.Part;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChainDriveGenerator extends SpecialBlockStateGen {

	private BiFunction<BlockState, String, ModelFile> modelFunc;

	public ChainDriveGenerator(BiFunction<BlockState, String, ModelFile> modelFunc) {
		this.modelFunc = modelFunc;
	}

	@Override
	protected int getXRotation(BlockState state) {
		ChainDriveBlock.Part part = state.getValue(ChainDriveBlock.PART);
		boolean connectedAlongFirst = state.getValue(ChainDriveBlock.CONNECTED_ALONG_FIRST_COORDINATE);
		Axis axis = state.getValue(ChainDriveBlock.AXIS);

		if (part == Part.NONE)
			return axis == Axis.Y ? 90 : 0;
		if (axis == Axis.X)
			return (connectedAlongFirst ? 90 : 0) + (part == Part.START ? 180 : 0);
		if (axis == Axis.Z)
			return (connectedAlongFirst ? 0 : (part == Part.START ? 270 : 90));
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		ChainDriveBlock.Part part = state.getValue(ChainDriveBlock.PART);
		boolean connectedAlongFirst = state.getValue(ChainDriveBlock.CONNECTED_ALONG_FIRST_COORDINATE);
		Axis axis = state.getValue(ChainDriveBlock.AXIS);

		if (part == Part.NONE)
			return axis == Axis.X ? 90 : 0;
		if (axis == Axis.Z)
			return (connectedAlongFirst && part == Part.END ? 270 : 90);
		boolean flip = part == Part.END && !connectedAlongFirst || part == Part.START && connectedAlongFirst;
		if (axis == Axis.Y)
			return (connectedAlongFirst ? 90 : 0) + (flip ? 180 : 0);
		return 0;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return modelFunc.apply(state, getModelSuffix(state));
	}

	protected String getModelSuffix(BlockState state) {
		ChainDriveBlock.Part part = state.getValue(ChainDriveBlock.PART);
		Axis axis = state.getValue(ChainDriveBlock.AXIS);

		if (part == Part.NONE)
			return "single";

		String orientation = axis == Axis.Y ? "vertical" : "horizontal";
		String section = part == Part.MIDDLE ? "middle" : "end";
		return section + "_" + orientation;
	}

}
