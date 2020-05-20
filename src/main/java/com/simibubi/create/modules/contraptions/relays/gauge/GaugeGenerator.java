package com.simibubi.create.modules.contraptions.relays.gauge;

import com.simibubi.create.foundation.utility.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraftforge.client.model.generators.ModelFile;

public class GaugeGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		Direction direction = state.get(GaugeBlock.FACING);
		boolean alongFirst = state.get(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

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
		Direction direction = state.get(GaugeBlock.FACING);
		boolean alongFirst = state.get(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

		if (direction.getAxis()
			.isVertical())
			return alongFirst ? 90 : 0;

		return ((int) direction.getHorizontalAngle() + 360 + 90) % 360;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		boolean vertical = state.get(GaugeBlock.FACING)
			.getAxis()
			.isVertical();
		String partial = vertical ? "base" : "base_wall";
		return prov.models()
			.getExistingFile(prov.modLoc("block/gauge/" + partial));
	}

}
