package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.content.logistics.chute.ChuteBlock.Shape;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChuteGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(BlockState state) {
		return 0;
	}

	@Override
	protected int getYRotation(BlockState state) {
		return horizontalAngle(state.getValue(ChuteBlock.FACING));
	}

	@Override
	public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
		BlockState state) {
		boolean horizontal = state.getValue(ChuteBlock.FACING) != Direction.DOWN;
		ChuteBlock.Shape shape = state.getValue(ChuteBlock.SHAPE);

		if (!horizontal)
			return shape == Shape.NORMAL ? AssetLookup.partialBaseModel(ctx, prov)
				: shape == Shape.INTERSECTION || shape == Shape.ENCASED
					? AssetLookup.partialBaseModel(ctx, prov, "intersection")
					: AssetLookup.partialBaseModel(ctx, prov, "windowed");
		return shape == Shape.INTERSECTION ? AssetLookup.partialBaseModel(ctx, prov, "diagonal", "intersection")
			: shape == Shape.ENCASED ? AssetLookup.partialBaseModel(ctx, prov, "diagonal", "encased")
				: AssetLookup.partialBaseModel(ctx, prov, "diagonal");
	}

}
