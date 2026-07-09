package com.simibubi.create.foundation.model;

import com.simibubi.create.foundation.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelData.Builder;

public abstract class BakedModelWrapperWithData extends BakedModelWrapper<BakedModel> {

	public BakedModelWrapperWithData(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public final ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData blockEntityData) {
		Builder builder = ModelData.builder();
		if (originalModel instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) originalModel).gatherModelData(builder, world, pos, state, blockEntityData);
		gatherModelData(builder, world, pos, state, blockEntityData);
		return builder.build();
	}

	protected abstract ModelData.Builder gatherModelData(ModelData.Builder builder, BlockAndTintGetter world,
		BlockPos pos, BlockState state, ModelData blockEntityData);

}
