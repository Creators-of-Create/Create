package com.simibubi.create.foundation.render;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

public class CutoutPartial extends PartialModel {
	public CutoutPartial(ResourceLocation modelLocation) {
		super(modelLocation);
	}

	@Override
	protected void set(BakedModel bakedModel) {
		super.set(new Wrapper(bakedModel));
	}

	private record Wrapper(BakedModel inner) implements BakedModel {
		private static final ChunkRenderTypeSet CUTOUT = ChunkRenderTypeSet.of(RenderType.cutout());

		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
			if (renderType == null) {
				return inner.getQuads(state, side, rand, data, null);
			} else if (renderType == RenderType.cutout()) {
				return inner.getQuads(state, side, rand, data, renderType);
			} else {
				return List.of();
			}
		}

		@Override
		public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
			return CUTOUT;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
			return inner.getQuads(pState, pDirection, pRandom);
		}

		@Override
		public boolean useAmbientOcclusion(BlockState state) {
			return inner.useAmbientOcclusion(state);
		}

		@Override
		public boolean useAmbientOcclusion(BlockState state, RenderType renderType) {
			return inner.useAmbientOcclusion(state, renderType);
		}

		@Override
		public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
			return inner.applyTransform(transformType, poseStack, applyLeftHandTransform);
		}

		@Override
		public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
			return inner.getModelData(level, pos, state, modelData);
		}

		@Override
		public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
			return inner.getParticleIcon(data);
		}

		@Override
		public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
			return inner.getRenderTypes(itemStack, fabulous);
		}

		@Override
		public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
			return inner.getRenderPasses(itemStack, fabulous);
		}

		@Override
		public boolean useAmbientOcclusion() {
			return inner.useAmbientOcclusion();
		}

		@Override
		public boolean isGui3d() {
			return inner.isGui3d();
		}

		@Override
		public boolean usesBlockLight() {
			return inner.usesBlockLight();
		}

		@Override
		public boolean isCustomRenderer() {
			return inner.isCustomRenderer();
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			return inner.getParticleIcon();
		}

		@Override
		public ItemOverrides getOverrides() {
			return inner.getOverrides();
		}
	}
}
