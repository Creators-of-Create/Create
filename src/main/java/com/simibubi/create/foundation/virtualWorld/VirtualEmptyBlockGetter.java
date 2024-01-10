package com.simibubi.create.foundation.virtualWorld;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public interface VirtualEmptyBlockGetter extends BlockAndTintGetter {
	public static final VirtualEmptyBlockGetter INSTANCE = new StaticLightImpl(0, 15);
	public static final VirtualEmptyBlockGetter FULL_BRIGHT = new StaticLightImpl(15, 15);
	public static final VirtualEmptyBlockGetter FULL_DARK = new StaticLightImpl(0, 0);

	public static boolean is(BlockAndTintGetter blockGetter) {
		return blockGetter instanceof VirtualEmptyBlockGetter;
	}

	@Override
	@Nullable
	default BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	default BlockState getBlockState(BlockPos pos) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	default FluidState getFluidState(BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	default int getHeight() {
		return 1;
	}

	@Override
	default int getMinBuildHeight() {
		return 0;
	}

	@Override
	default float getShade(Direction direction, boolean shaded) {
		return 1f;
	}

	@Override
	default int getBlockTint(BlockPos pos, ColorResolver resolver) {
		Biome plainsBiome = Minecraft.getInstance().getConnection().registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
		return resolver.getColor(plainsBiome, pos.getX(), pos.getZ());
	}

	public static class StaticLightImpl implements VirtualEmptyBlockGetter {
		private final LevelLightEngine lightEngine;

		public StaticLightImpl(int blockLight, int skyLight) {
			lightEngine = new LevelLightEngine(new LightChunkGetter() {
				@Override
				@Nullable
				public LightChunk getChunkForLighting(int p_63023_, int p_63024_) {
					return null;
				}

				@Override
				public BlockGetter getLevel() {
					return StaticLightImpl.this;
				}
			}, false, false) {
				private final LayerLightEventListener blockListener = createStaticListener(blockLight);
				private final LayerLightEventListener skyListener = createStaticListener(skyLight);

				@Override
				public LayerLightEventListener getLayerListener(LightLayer layer) {
					return layer == LightLayer.BLOCK ? blockListener : skyListener;
				}
			};
		}

		private static LayerLightEventListener createStaticListener(int light) {
			return new LayerLightEventListener() {
				@Override
				public void checkBlock(BlockPos pos) {
				}

				@Override
				public boolean hasLightWork() {
					return false;
				}

				@Override
				public int runLightUpdates() {
					return 0;
				}

				@Override
				public void updateSectionStatus(SectionPos pos, boolean isSectionEmpty) {
				}

				@Override
				public void setLightEnabled(ChunkPos pos, boolean lightEnabled) {
				}

				@Override
				public void propagateLightSources(ChunkPos pos) {
				}

				@Override
				public DataLayer getDataLayerData(SectionPos pos) {
					return null;
				}

				@Override
				public int getLightValue(BlockPos pos) {
					return light;
				}
			};
		}

		@Override
		public LevelLightEngine getLightEngine() {
			return lightEngine;
		}
	}
}
