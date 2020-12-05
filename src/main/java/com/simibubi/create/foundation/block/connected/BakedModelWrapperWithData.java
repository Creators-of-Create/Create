package com.simibubi.create.foundation.block.connected;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;

public abstract class BakedModelWrapperWithData extends BakedModelWrapper<IBakedModel> {

	public BakedModelWrapperWithData(IBakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public final IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData) {
		Builder builder = new ModelDataMap.Builder();
		if (originalModel instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) originalModel).gatherModelData(builder, world, pos, state);
		return gatherModelData(builder, world, pos, state).build();
	}

	protected abstract ModelDataMap.Builder gatherModelData(ModelDataMap.Builder builder, IBlockDisplayReader world,
		BlockPos pos, BlockState state);

}
