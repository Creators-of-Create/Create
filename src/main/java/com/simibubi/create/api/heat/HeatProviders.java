package com.simibubi.create.api.heat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.heat.HeatDataMap;
import com.simibubi.create.foundation.utility.AttachedRegistry;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber
public class HeatProviders extends SavedData {
	private static final AttachedRegistry<Block, HeatProvider> HEAT_PROVIDERS = new AttachedRegistry<>(ForgeRegistries.BLOCKS);
	private static final String KEY = "create_heat";
	private static final String DATA_KEY = "heat_data";
	private final HeatDataMap data = new HeatDataMap();
	private final Level level;

	protected HeatProviders(Level level) {
		this.level = level;
	}

	protected HeatProviders(Level level, CompoundTag tag) {
		this(level);
		// Load data if present
		if (tag.contains(DATA_KEY)) {
			data.deserializeNBT(level, tag.getCompound(DATA_KEY));
		}
	}

	@Override
	public CompoundTag save(CompoundTag pCompoundTag) {
		pCompoundTag.put(DATA_KEY, this.data.serializeNBT());
		return pCompoundTag;
	}

	public static HeatProviders load(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(tag -> load(level, tag), () -> new HeatProviders(level), KEY);
	}

	protected static HeatProviders load(Level level, CompoundTag tag) {
		return new HeatProviders(level, tag);
	}

	public static void registerHeatProvider(Block block, HeatLevelCalculation heatLevel) {
		HeatProvider provider = new HeatProvider(heatLevel);
		HEAT_PROVIDERS.register(block, provider);
	}

	public static void registerHeatProvider(Block block, HeatLevelCalculation heatLevel, HeatedAreaOverride areaOverride) {
		HeatProvider provider = new HeatProvider(heatLevel, areaOverride);
		HEAT_PROVIDERS.register(block, provider);
	}

	public static void registerHeatProvider(Block block, HeatLevelCalculation heatLevel, HeatedAreaOverride areaOverride, MaxHeatConsumerOverride maxConsumers) {
		HeatProvider provider = new HeatProvider(heatLevel, areaOverride, maxConsumers);
		HEAT_PROVIDERS.register(block, provider);
	}

	public static boolean isHeatProvider(Block block) {
		return HEAT_PROVIDERS.get(block) != null;
	}

	public static boolean isHeatProvider(BlockState state) {
		return isHeatProvider(state.getBlock());
	}

	@Nullable
	public static HeatProvider getHeatProviderOf(BlockState state) {
		return getHeatProviderOf(state.getBlock());
	}

	@Nullable
	public static HeatProvider getHeatProviderOf(Block block) {
		return HEAT_PROVIDERS.get(block);
	}

	public boolean isHeatProvider(BlockPos pos) {
		return this.data.containsProviderAt(pos);
	}

	protected void addHeatProvider(BlockPos pos, HeatProvider provider) {
		this.data.storeProvider(pos, provider, new HashSet<>());
		// search for consumers in range without a provider
		Iterator<BlockPos> consumers = this.data.getUnheatedConsumers().iterator();
		while (consumers.hasNext()) {
			BlockPos consumerPos = consumers.next();

			if (!provider.getHeatedArea(this.level, pos).isInside(consumerPos)) continue;
			BlockState consumerState = this.level.getBlockState(consumerPos);

			if (!(consumerState.getBlock() instanceof HeatConsumer consumer)) {
				// Remove invalid consumers
				Create.LOGGER.warn("Removed invalid pending heat consumer at {}", consumerPos);
				consumers.remove();
				continue;
			}

			if (!consumer.isValidSource(this.level, provider, pos, consumerPos)) continue;

			if (!addHeatConsumer(consumerPos, consumer)) continue;
			// Success
			consumers.remove();
			break;
		}

		setDirty();
	}

	public HeatLevel getHeatFor(BlockPos consumerPos) {
		// No heat if no provider
		if (this.data.getUnheatedConsumers().contains(consumerPos)) return HeatLevel.NONE;

		for (Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>> entry : this.data.getActiveHeatProviders()) {
			Set<BlockPos> consumers = entry.getValue().getSecond();
			// Skip is block pos is not added as consumer pos
			if (!consumers.contains(consumerPos)) continue;
			HeatProvider provider = entry.getValue().getFirst();
			HeatLevel heatLevel = provider.getHeatLevel(this.level, entry.getKey(), consumerPos);
			return heatLevel;
		}

		return HeatLevel.NONE;
	}

	public static void registerDefaults() {
		registerHeatProvider(AllBlocks.BLAZE_BURNER.get(), (level1, providerPos, consumerPos) -> BlazeBurnerBlock.getHeatLevelOf(level1.getBlockState(providerPos)));
		registerHeatProvider(AllBlocks.LIT_BLAZE_BURNER.get(), (level1, providerPos, consumerPos) -> HeatLevel.SMOULDERING);
	}

	public void removeHeatProvider(BlockPos pos) {
		Pair<HeatProvider, Set<BlockPos>> removedEntry = this.data.removeHeatProvider(pos);
		if (removedEntry == null) return;
		this.data.getUnheatedConsumers().addAll(removedEntry.getSecond());
		setDirty();
	}

	/**
	 * Adds a heat consumer to the nearest valid heat provider.
	 *
	 * @return true if successfully added
	 */
	public boolean addHeatConsumer(BlockPos consumer) {
		BlockState consumerState = this.level.getBlockState(consumer);
		if (consumerState.getBlock() instanceof HeatConsumer heatConsumer) {
			return addHeatConsumer(consumer, heatConsumer);
		}
		return false;
	}

	/**
	 * Adds a heat consumer to the nearest valid heat provider.
	 *
	 * @return true if successfully added
	 */
	public boolean addHeatConsumer(BlockPos consumerPosition, HeatConsumer consumer) {
		// Get the closest possible heat provider
		Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>> closestProviderEntry = null;

		for (Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>> entry : this.data.getActiveHeatProviders()) {
			BlockPos providerPosition = entry.getKey();
			HeatProvider provider = entry.getValue().getFirst();
			Set<BlockPos> consumerSet = entry.getValue().getSecond();
			// Skip if consumer is out of range
			if (!provider.isInHeatedRange(this.level, providerPosition, consumerPosition)) continue;
			// Skip if the provider has no capacity left
			if (consumerSet.size() + 1 > provider.getMaxConsumers(this.level, providerPosition)) continue;
			// Skip is the provider is not a valid source for the consumer
			if (!consumer.isValidSource(this.level, provider, providerPosition, consumerPosition)) continue;
			if (closestProviderEntry == null) {
				closestProviderEntry = entry;
				continue;
			}
			double closestDistance = closestProviderEntry.getKey().distSqr(consumerPosition);
			double currentDistance = providerPosition.distSqr(consumerPosition);
			// Skip if the provider is further away
			if (currentDistance >= closestDistance) continue;
			closestProviderEntry = entry;
		}

		// Exit if no valid provider exists
		if (closestProviderEntry == null) {
			this.data.getUnheatedConsumers().add(consumerPosition);
			setDirty();
			return false;
		}

		return addConsumerToProvider(closestProviderEntry.getKey(), closestProviderEntry.getValue().getFirst(), closestProviderEntry.getValue().getSecond(), consumerPosition, consumer);
	}

	protected boolean addConsumerToProvider(BlockPos providerPos, HeatProvider provider, Set<BlockPos> consumerSet, BlockPos consumerPos, HeatConsumer consumer) {
		if (consumerSet.add(consumerPos)) {
			consumer.onHeatProvided(this.level, provider, providerPos, consumerPos);
			setDirty();
			return true;
		}

		setDirty();
		return false;
	}

	public void removeHeatConsumer(BlockPos pos) {
		for (Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>> entry : this.data.getActiveHeatProviders()) {
			Set<BlockPos> consumers = entry.getValue().getSecond();
			if (consumers.remove(pos)) setDirty();
		}
	}

	@SubscribeEvent
	static void onHeatProviderPlaced(final BlockEvent.NeighborNotifyEvent e) {
		// Exit if the World is not server side
		if (!(e.getWorld() instanceof ServerLevel level)) return;
		// Add Heat Provider
		HeatProviders provider = load(level);
		BlockState state = e.getState();
		// Exit if no heat provider is registered for this block
		if (!isHeatProvider(state)) return;
		HeatProvider heatProvider = HEAT_PROVIDERS.get(state.getBlock());
		provider.addHeatProvider(e.getPos(), heatProvider);
		provider.setDirty();
	}

	@SubscribeEvent
	static void onHeatProviderRemoved(final BlockEvent.NeighborNotifyEvent e) {
		// Exit if the World is not server side
		if (!(e.getWorld() instanceof ServerLevel level)) return;
		// Add Heat Provider
		// Exit if the provider is at the location
		if (isHeatProvider(e.getState())) return;
		// Remove heat provider
		HeatProviders provider = load(level);
		provider.removeHeatProvider(e.getPos());
		provider.setDirty();
	}

	@FunctionalInterface
	public interface HeatLevelCalculation {
		HeatLevel getHeatLevel(Level level, BlockPos providerPos, BlockPos consumerPos);
	}

	@FunctionalInterface
	public interface HeatedAreaOverride {
		BoundingBox getHeatedArea(Level level, BlockPos providerPos);
	}

	@FunctionalInterface
	public interface MaxHeatConsumerOverride {
		int getMaxHeatConsumers(Level level, BlockPos providerPos);
	}

	/**
	 * Internal Helper class to handle easily handle actions
	 */
	public static class HeatProvider {
		private final HeatLevelCalculation levelCalculation;
		private final HeatedAreaOverride areaOverride;
		private final MaxHeatConsumerOverride maxHeatConsumerOverride;

		protected HeatProvider(HeatLevelCalculation heatLevelCalculation) {
			this(heatLevelCalculation, (level1, providerPos) -> new BoundingBox(providerPos.above()), (level1, providerPos) -> 1);
		}

		protected HeatProvider(HeatLevelCalculation heatLevelCalculation, HeatedAreaOverride areaOverride) {
			this(heatLevelCalculation, areaOverride, (level1, providerPos) -> 1);
		}

		protected HeatProvider(HeatLevelCalculation heatLevelCalculation, HeatedAreaOverride areaOverride, MaxHeatConsumerOverride maxHeatConsumerOverride) {
			this.levelCalculation = heatLevelCalculation;
			this.areaOverride = areaOverride;
			this.maxHeatConsumerOverride = maxHeatConsumerOverride;
		}

		public HeatLevel getHeatLevel(Level level, BlockPos providerPos, BlockPos consumerPos) {
			return levelCalculation.getHeatLevel(level, providerPos, consumerPos);
		}

		public BoundingBox getHeatedArea(Level level, BlockPos providerPos) {
			return areaOverride.getHeatedArea(level, providerPos);
		}

		public int getMaxConsumers(Level level, BlockPos providerPos) {
			return maxHeatConsumerOverride.getMaxHeatConsumers(level, providerPos);
		}

		public boolean isInHeatedRange(Level level, BlockPos providerPos, BlockPos consumerPos) {
			return getHeatedArea(level, providerPos).isInside(consumerPos);
		}
	}
}
