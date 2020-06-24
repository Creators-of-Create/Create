package com.simibubi.create.content.logistics.block.chute;

import com.simibubi.create.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChuteGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.get(ChuteBlock.FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		boolean horizontal = state.get(ChuteBlock.FACING) != Direction.DOWN;
		ChuteBlock.Shape shape = state.get(ChuteBlock.SHAPE);

		if (!horizontal)
			return shape == Shape.NORMAL ? AssetLookup.partialBaseModel(ctx, prov)
				: AssetLookup.partialBaseModel(ctx, prov, "windowed");

		return shape == Shape.NORMAL ? AssetLookup.partialBaseModel(ctx, prov, "diagonal")
			: shape == Shape.START ? AssetLookup.partialBaseModel(ctx, prov, "diagonal", "start")
				: AssetLookup.partialBaseModel(ctx, prov, "diagonal", "straight");
	}

}
