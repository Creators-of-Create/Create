package com.simibubi.create.api.heat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
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
		return this.data.containsKey(pos);
	}

	protected void addHeatProvider(BlockPos pos, HeatProvider provider) {
		this.data.put(pos, provider, new HashSet<>());
		// search for consumers in range without a provider
		Iterator<BlockPos> consumers = this.data.getUnheatedConsumers().iterator();
		while (consumers.hasNext()) {
			BlockPos consumerPos = consumers.next();
			if (provider.getHeatedArea(this.level, pos).isInside(consumerPos)) {
				BlockState consumerState = this.level.getBlockState(consumerPos);
				if (consumerState.getBlock() instanceof HeatConsumer consumer) {
					if (consumer.isValidSource(this.level, provider, pos, consumerPos)) {
						if (addHeatConsumer(consumerPos, consumer)) {
							// Success
							consumers.remove();
							break;
						}
					}
				} else {
					// Remove invalid consumers
					Create.LOGGER.warn("Removed invalid pending heat consumer at {}", consumerPos);
					consumers.remove();
				}
			}
		}

		setDirty();
	}

	public HeatLevel getHeatFor(BlockPos consumerPos) {
		// No heat if no provider
		if (this.data.getUnheatedConsumers().contains(consumerPos)) return HeatLevel.NONE;

		return this.data.entrySet()
				.stream()
				.filter(entry -> {
					Set<BlockPos> consumers = entry.getValue().getSecond();
					return consumers.contains(consumerPos);
				})
				.findFirst()
				.map(entry -> entry.getValue().getFirst().getHeatLevel(this.level, entry.getKey(), consumerPos))
				.orElse(HeatLevel.NONE);
	}

	public static void registerDefaults() {
		registerHeatProvider(AllBlocks.BLAZE_BURNER.get(), (level1, providerPos, consumerPos) -> BlazeBurnerBlock.getHeatLevelOf(level1.getBlockState(providerPos)));
		registerHeatProvider(AllBlocks.LIT_BLAZE_BURNER.get(), (level1, providerPos, consumerPos) -> HeatLevel.SMOULDERING);
	}

	public void removeHeatProvider(BlockPos pos) {
		Pair<HeatProvider, Set<BlockPos>> removedEntry = this.data.remove(pos);
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
		Optional<Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>>> possibleProvider = this.data.entrySet()
				.stream()
				.filter(entry -> entry.getValue().getFirst().isInHeatedRange(this.level, entry.getKey(), consumerPosition))
				.filter(entry -> entry.getValue().getSecond().size() + 1 <= entry.getValue().getFirst().getMaxConsumers(this.level, entry.getKey()))
				.filter(entry -> consumer.isValidSource(this.level, entry.getValue().getFirst(), entry.getKey(), consumerPosition))
				.min((o1, o2) -> {
					double distance1 = o1.getKey().distSqr(consumerPosition);
					double distance2 = o2.getKey().distSqr(consumerPosition);
					return Double.compare(distance1, distance2);
				});
		// Exit if no valid provider exists
		if (possibleProvider.isEmpty()) {
			this.data.getUnheatedConsumers().add(consumerPosition);
			setDirty();
			return false;
		}

		Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>> providerEntry = possibleProvider.get();
		return addConsumerToProvider(providerEntry.getKey(), providerEntry.getValue().getFirst(), providerEntry.getValue().getSecond(), consumerPosition, consumer);
	}

	protected boolean addConsumerToProvider(BlockPos providerPos, HeatProvider provider, Set<BlockPos> consumerSet, BlockPos consumerPos, HeatConsumer consumer) {
		setDirty();
		if (consumerSet.add(consumerPos)) {
			consumer.onHeatProvided(this.level, provider, providerPos, consumerPos);
			return true;
		}

		return false;
	}

	public void removeHeatConsumer(BlockPos pos) {
		this.data.entrySet().stream()
				.filter(entry -> {
					Set<BlockPos> consumers = entry.getValue().getSecond();
					return consumers.contains(pos);
				})
				.findFirst()
				.ifPresent(entry -> entry.getValue().getSecond().remove(pos));
		setDirty();
	}

	@SubscribeEvent
	static void onHeatProviderPlaced(final BlockEvent.NeighborNotifyEvent e) {
		BlockState state = e.getState();
		HeatProvider heatProvider = HEAT_PROVIDERS.get(state.getBlock());
		// Exit if no heat provider is registered for this block
		if (isHeatProvider(state)) return;
		// Exit if the World is not server side
		if (!(e.getWorld() instanceof ServerLevel level)) return;
		// Add Heat Provider
		HeatProviders provider = load(level);
		provider.addHeatProvider(e.getPos(), heatProvider);
	}

	@SubscribeEvent
	static void onHeatProviderRemoved(final BlockEvent.NeighborNotifyEvent e) {
		// Exit if the World is not server side
		if (!(e.getWorld() instanceof ServerLevel level)) return;
		// Add Heat Provider
		HeatProviders provider = load(level);
		// Exit if no provider was at the location
		if (!provider.isHeatProvider(e.getPos())) return;
		// Remove heat provider
		provider.removeHeatConsumer(e.getPos());
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
