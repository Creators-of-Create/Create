package com.simibubi.create.foundation.block.connected;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;

public abstract class BakedModelWrapperWithData extends BakedModelWrapper<BakedModel> {

	public BakedModelWrapperWithData(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public final IModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, IModelData tileData) {
		Builder builder = new ModelDataMap.Builder();
		if (originalModel instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) originalModel).gatherModelData(builder, world, pos, state);
		return gatherModelData(builder, world, pos, state).build();
	}

	protected abstract ModelDataMap.Builder gatherModelData(ModelDataMap.Builder builder, BlockAndTintGetter world,
		BlockPos pos, BlockState state);

}
