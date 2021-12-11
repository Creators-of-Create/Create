package com.simibubi.create.foundation.utility.worldWrappers.chunk;

import com.simibubi.create.lib.mixin.accessor.DimensionTypeAccessor;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

public class EmptierChunk extends LevelChunk {

	private static final class DummyLevel extends Level {
		RegistryAccess access;

		private DummyLevel(WritableLevelData p_46450_, ResourceKey<Level> p_46451_, DimensionType p_46452_,
			Supplier<ProfilerFiller> p_46453_, boolean p_46454_, boolean p_46455_, long p_46456_) {
			super(p_46450_, p_46451_, p_46452_, p_46453_, p_46454_, p_46455_, p_46456_);
		}

		public Level withAccess(RegistryAccess access) {
			this.access = access;
			return this;
		}

		@Override
		public ChunkSource getChunkSource() {
			return null;
		}

		@Override
		public void levelEvent(Player pPlayer, int pType, BlockPos pPos, int pData) {}

		@Override
		public void gameEvent(Entity pEntity, GameEvent pEvent, BlockPos pPos) {}

		@Override
		public RegistryAccess registryAccess() {
			return access;
		}

		@Override
		public List<? extends Player> players() {
			return null;
		}

		@Override
		public Biome getUncachedNoiseBiome(int pX, int pY, int pZ) {
			return null;
		}

		@Override
		public float getShade(Direction pDirection, boolean pShade) {
			return 0;
		}

		@Override
		public void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags) {}

		@Override
		public void playSound(Player pPlayer, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory,
			float pVolume, float pPitch) {}

		@Override
		public void playSound(Player pPlayer, Entity pEntity, SoundEvent pEvent, SoundSource pCategory, float pVolume,
			float pPitch) {}

		@Override
		public String gatherChunkSourceStats() {
			return null;
		}

		@Override
		public Entity getEntity(int pId) {
			return null;
		}

		@Override
		public MapItemSavedData getMapData(String pMapName) {
			return null;
		}

		@Override
		public void setMapData(String pMapId, MapItemSavedData pData) {}

		@Override
		public int getFreeMapId() {
			return 0;
		}

		@Override
		public void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress) {}

		@Override
		public Scoreboard getScoreboard() {
			return null;
		}

		@Override
		public RecipeManager getRecipeManager() {
			return null;
		}

		@Override
		public TagContainer getTagManager() {
			return null;
		}

		@Override
		protected LevelEntityGetter<Entity> getEntities() {
			return null;
		}

		@Override
		public LevelTickAccess<Block> getBlockTicks() {
			return BlackholeTickAccess.emptyLevelList();
		}

		@Override
		public LevelTickAccess<Fluid> getFluidTicks() {
			return BlackholeTickAccess.emptyLevelList();
		}
	}

	private static final DummyLevel DUMMY_LEVEL = new DummyLevel(null, null,
			DimensionTypeAccessor.getDEFAULT_OVERWORLD(), null, false, false, 0);

	public EmptierChunk(RegistryAccess registryAccess) {
		super(DUMMY_LEVEL.withAccess(registryAccess), null);
	}

	public BlockState getBlockState(BlockPos p_180495_1_) {
		return Blocks.VOID_AIR.defaultBlockState();
	}

	@Nullable
	public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
		return null;
	}

	public FluidState getFluidState(BlockPos p_204610_1_) {
		return Fluids.EMPTY.defaultFluidState();
	}

	public int getLightEmission(BlockPos p_217298_1_) {
		return 0;
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos p_177424_1_, LevelChunk.EntityCreationType p_177424_2_) {
		return null;
	}

	public void addAndRegisterBlockEntity(BlockEntity p_150813_1_) {}

	public void setBlockEntity(BlockEntity p_177426_2_) {}

	public void removeBlockEntity(BlockPos p_177425_1_) {}

	public void markUnsaved() {}

	public boolean isEmpty() {
		return true;
	}

	public boolean isYSpaceEmpty(int p_76606_1_, int p_76606_2_) {
		return true;
	}

	public ChunkHolder.FullChunkStatus getFullStatus() {
		return ChunkHolder.FullChunkStatus.BORDER;
	}
}
