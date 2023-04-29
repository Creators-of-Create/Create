package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class SmartObserverGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return switch (state.getValue(SmartObserverBlock.TARGET)) {
		case CEILING -> -90;
		case WALL -> 0;
		case FLOOR -> 90;
		};
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(ThresholdSwitchBlock.FACING)) + 180;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return AssetLookup.forPowered(ctx, prov)
			.apply(state);
	}

}
