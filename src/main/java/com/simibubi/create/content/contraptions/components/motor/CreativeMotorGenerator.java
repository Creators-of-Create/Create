package com.simibubi.create.content.contraptions.components.motor;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class CreativeMotorGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return state.get(CreativeMotorBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return state.get(CreativeMotorBlock.FACING)
			.getAxis()
			.isVertical() ? 0 : horizontalAngle(state.get(CreativeMotorBlock.FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return state.get(CreativeMotorBlock.FACING)
			.getAxis()
			.isVertical() ? AssetLookup.partialBaseModel(ctx, prov, "vertical")
				: AssetLookup.partialBaseModel(ctx, prov);
	}

}
