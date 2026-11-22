package com.simibubi.create.content.kinetics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objenesis.ObjenesisStd;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;

/**
 * Fake level that only knows which block entity sits at which position. Created without running the
 * {@link Level} constructor, so it does not need a bootstrapped game. Everything else is unsupported.
 */
public abstract class FakeLevel extends Level {

	private Map<BlockPos, BlockEntity> blockEntities;

	private FakeLevel() {
		super(null, null, null, null, null, false, false, 0, 0);
	}

	public static FakeLevel create() {
		FakeLevel level = new ObjenesisStd().newInstance(Impl.class);
		level.blockEntities = new HashMap<>();
		return level;
	}

	public void putBlockEntity(BlockEntity blockEntity) {
		blockEntities.put(blockEntity.getBlockPos(), blockEntity);
	}

	public void removeBlockEntity(BlockPos pos) {
		blockEntities.remove(pos);
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return blockEntities.get(pos);
	}

	private static UnsupportedOperationException unsupported() {
		return new UnsupportedOperationException("Not supported by FakeLevel");
	}

	private static final class Impl extends FakeLevel {
		@Override
		protected LevelEntityGetter<Entity> getEntities() {
			throw unsupported();
		}

		@Override
		public float getShade(Direction direction, boolean shade) {
			throw unsupported();
		}

		@Override
		public float getDayTimeFraction() {
			throw unsupported();
		}

		@Override
		public float getDayTimePerTick() {
			throw unsupported();
		}

		@Override
		public String gatherChunkSourceStats() {
			throw unsupported();
		}

		@Override
		public List<? extends Player> players() {
			throw unsupported();
		}

		@Override
		public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
			throw unsupported();
		}

		@Override
		public TickRateManager tickRateManager() {
			throw unsupported();
		}

		@Override
		public Entity getEntity(int id) {
			throw unsupported();
		}

		@Override
		public FeatureFlagSet enabledFeatures() {
			throw unsupported();
		}

		@Override
		public PotionBrewing potionBrewing() {
			throw unsupported();
		}

		@Override
		public RecipeManager getRecipeManager() {
			throw unsupported();
		}

		@Override
		public ChunkSource getChunkSource() {
			throw unsupported();
		}

		@Override
		public MapId getFreeMapId() {
			throw unsupported();
		}

		@Override
		public MapItemSavedData getMapData(MapId mapId) {
			throw unsupported();
		}

		@Override
		public Scoreboard getScoreboard() {
			throw unsupported();
		}

		@Override
		public LevelTickAccess<Block> getBlockTicks() {
			throw unsupported();
		}

		@Override
		public LevelTickAccess<Fluid> getFluidTicks() {
			throw unsupported();
		}

		@Override
		public void destroyBlockProgress(int breakerId, BlockPos pos, int progress) {
			throw unsupported();
		}

		@Override
		public void playSeededSound(Player player, double x, double y, double z, Holder<SoundEvent> sound,
			SoundSource category, float volume, float pitch, long seed) {
			throw unsupported();
		}

		@Override
		public void playSeededSound(Player player, Entity entity, Holder<SoundEvent> sound, SoundSource category,
			float volume, float pitch, long seed) {
			throw unsupported();
		}

		@Override
		public void sendBlockUpdated(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
			throw unsupported();
		}

		@Override
		public void setDayTimeFraction(float dayTimeFraction) {
			throw unsupported();
		}

		@Override
		public void setDayTimePerTick(float dayTimePerTick) {
			throw unsupported();
		}

		@Override
		public void setMapData(MapId mapId, MapItemSavedData mapData) {
			throw unsupported();
		}

		@Override
		public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
			throw unsupported();
		}

		@Override
		public void levelEvent(Player player, int type, BlockPos pos, int data) {
			throw unsupported();
		}
	}
}
