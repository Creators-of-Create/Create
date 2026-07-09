package com.simibubi.create.foundation.model;

import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import com.simibubi.create.foundation.model.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.simibubi.create.foundation.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * Temporary Create 26.2 porting bridge for legacy model wrappers.
 * TODO 26.2: Replace with DelegateBlockStateModel or ItemModel wrappers.
 */
@Deprecated(forRemoval = true)
public class BakedModelWrapper<T extends BakedModel> implements BakedModel {
	protected final T originalModel;

	public BakedModelWrapper(T originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
		return originalModel.getQuads(state, side, rand);
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
		RenderType renderType) {
		return originalModel.getQuads(state, side, rand, data, renderType);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return originalModel.useAmbientOcclusion();
	}

	@Override
	public boolean usesBlockLight() {
		return originalModel.usesBlockLight();
	}

	@Override
	public boolean isGui3d() {
		return originalModel.isGui3d();
	}

	@Override
	public boolean isCustomRenderer() {
		return originalModel.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon(ModelData data) {
		return originalModel.getParticleIcon(data);
	}

	@Override
	public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
		return originalModel.getModelData(level, pos, state, modelData);
	}

	@Override
	public Object getTransforms() {
		return originalModel.getTransforms();
	}

	@Override
	public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
		return originalModel.getRenderPasses(stack, fabulous);
	}

	@Override
	public Set<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
		return originalModel.getRenderTypes(stack, fabulous);
	}

	@Override
	public Set<RenderType> getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		return originalModel.getRenderTypes(state, rand, data);
	}
}
