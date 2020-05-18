package com.simibubi.create.modules.contraptions.components.motor;

import com.simibubi.create.foundation.utility.data.AssetLookup;
import com.simibubi.create.foundation.utility.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class MotorGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.get(MotorBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return state.get(MotorBlock.FACING)
			.getAxis()
			.isVertical() ? 0
				: (int) state.get(MotorBlock.FACING)
					.getHorizontalAngle();
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return state.get(MotorBlock.FACING)
			.getAxis()
			.isVertical() ? AssetLookup.partialBaseModel(ctx, prov, "vertical")
				: AssetLookup.partialBaseModel(ctx, prov);
	}

}
