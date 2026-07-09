package com.simibubi.create.content.logistics.packager;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateBlockModelGenerator;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.tterrag.registrate.providers.generators.ModelFile;

public class PackagerGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(PackagerBlock.FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockModelGenerator prov,
												BlockState state) {
		String suffix = state.getOptionalValue(PackagerBlock.LINKED)
			.orElse(false) ? "linked" : state.getValue(PackagerBlock.POWERED) ? "powered" : "";
		return state.getValue(PackagerBlock.FACING)
			.getAxis() == Axis.Y ? AssetLookup.partialBaseModel(ctx, prov, "vertical", suffix)
				: AssetLookup.partialBaseModel(ctx, prov, suffix);
	}

}
