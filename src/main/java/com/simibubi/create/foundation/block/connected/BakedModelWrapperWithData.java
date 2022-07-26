package com.simibubi.create.foundation.block.connected;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelData.Builder;

public abstract class BakedModelWrapperWithData extends BakedModelWrapper<BakedModel> {

	public BakedModelWrapperWithData(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public final ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData) {
		Builder builder = ModelData.builder();
		if (originalModel instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) originalModel).gatherModelData(builder, world, pos, state);
		return gatherModelData(builder, world, pos, state).build();
	}

	protected abstract ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
		BlockPos pos, BlockState state);

}
