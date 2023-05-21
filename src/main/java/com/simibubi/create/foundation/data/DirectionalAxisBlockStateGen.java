package com.simibubi.create.foundation.data;

import com.simibubi.create.content.kinetics.gauge.GaugeBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class DirectionalAxisBlockStateGen extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		Direction direction = state.getValue(GaugeBlock.FACING);
		boolean alongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

		if (direction == Direction.DOWN)
			return 180;
		if (direction == Direction.UP)
			return 0;
		if ((direction.getAxis() == Axis.X) == alongFirst)
			return 90;

		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		Direction direction = state.getValue(GaugeBlock.FACING);
		boolean alongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

		if (direction.getAxis()
			.isVertical())
			return alongFirst ? 90 : 0;

		return horizontalAngle(direction) + 90;
	}

	public abstract <T extends Block> String getModelPrefix(DataGenContext<Block, T> ctx,
		RegistrateBlockstateProvider prov, BlockState state);

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		boolean vertical = state.getValue(GaugeBlock.FACING)
			.getAxis()
			.isVertical();
		String partial = vertical ? "" : "_wall";
		return prov.models()
			.getExistingFile(prov.modLoc(getModelPrefix(ctx, prov, state) + partial));
	}

}
