package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class FlywheelGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(FlywheelBlock.HORIZONTAL_FACING)) + 90;
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		return prov.models()
			.getExistingFile(prov.modLoc("block/" + ctx.getName() + "/casing_" + state.getValue(FlywheelBlock.CONNECTION)
				.getSerializedName()));
	}
}
