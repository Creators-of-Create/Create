package com.simibubi.create.foundation.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

/**
 * Temporary Create 26.2 porting bridge for legacy baked-model callers.
 * TODO 26.2: Migrate these call sites to BlockStateModel, BlockModel, and ItemModel.
 */
@Deprecated(forRemoval = true)
public interface BakedModel extends BlockStateModel {
	default List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand) {
		return Collections.emptyList();
	}

	default List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
		RenderType renderType) {
		return getQuads(state, side, rand);
	}

	default boolean useAmbientOcclusion() {
		return true;
	}

	default boolean usesBlockLight() {
		return true;
	}

	default boolean isGui3d() {
		return true;
	}

	default boolean isCustomRenderer() {
		return false;
	}

	default TextureAtlasSprite getParticleIcon(ModelData data) {
		return null;
	}

	default Object getTransforms() {
		return null;
	}

	default ItemOverrides getOverrides() {
		return ItemOverrides.EMPTY;
	}

	default List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
		return List.of(this);
	}

	default Set<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
		return Collections.emptySet();
	}

	default Set<RenderType> getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
		return Collections.emptySet();
	}

	default ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
		return modelData;
	}

	@Override
	default void collectParts(RandomSource random, List<BlockStateModelPart> output) {
	}

	@Override
	default Material.Baked particleMaterial() {
		return null;
	}

	@Override
	default int materialFlags() {
		return 0;
	}
}
