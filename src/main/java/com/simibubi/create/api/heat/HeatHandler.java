package com.simibubi.create.api.heat;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.HeatDataMap;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

public class HeatHandler extends SavedData {
	private static final String KEY = "create_heat";
	private static final String DATA_KEY = "create_heat_data";
	private final HeatDataMap data = new HeatDataMap();
	private final Set<BlockPos> unheatedConsumers = new HashSet<>();
	private final Level level;

	protected HeatHandler(Level level) {
		this.level = level;
	}

	protected HeatHandler(Level level, CompoundTag tag) {
		this(level);
		// Load data if present
		if (tag.contains(DATA_KEY)) {
			data.deserializeNBT(level, tag.getList(DATA_KEY, Tag.TAG_COMPOUND));
		}
	}

	@Override
	public CompoundTag save(CompoundTag pCompoundTag) {
		pCompoundTag.put(DATA_KEY, this.data.serializeNBT());
		return pCompoundTag;
	}

	public void addHeatProvider(BlockPos pos, IHeatProvider provider) {
		this.data.put(pos, provider, new HashSet<>());
		// search for consumers in range without a provider
		Iterator<BlockPos> consumers = this.unheatedConsumers.iterator();
		while (consumers.hasNext()) {
			BlockPos consumerPos = consumers.next();
			if (provider.getHeatedArea(this.level, pos).isInside(consumerPos)) {
				BlockState consumerState = this.level.getBlockState(consumerPos);
				if (consumerState.getBlock() instanceof IHeatConsumer consumer) {
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

	/**
	 * Adds a heat consumer to the nearest valid heat provider.
	 *
	 * @return true if successfully added
	 */
	public boolean addHeatConsumer(BlockPos consumer) {
		BlockState consumerState = this.level.getBlockState(consumer);
		if (consumerState.getBlock() instanceof IHeatConsumer heatConsumer) {
			return addHeatConsumer(consumer, heatConsumer);
		}
		return false;
	}

	/**
	 * Adds a heat consumer to the nearest valid heat provider.
	 *
	 * @return true if successfully added
	 */
	public boolean addHeatConsumer(BlockPos consumerPosition, IHeatConsumer consumer) {
		// Get the closest possible heat provider
		Optional<Entry<BlockPos, Pair<IHeatProvider, Set<BlockPos>>>> possibleProvider = this.data.entrySet()
				.stream()
				.filter(entry -> entry.getValue().getFirst().isInHeatRange(this.level, entry.getKey(), consumerPosition))
				.filter(entry -> entry.getValue().getSecond().size() + 1 <= entry.getValue().getFirst().getMaxHeatConsumers(this.level, entry.getKey()))
				.filter(entry -> consumer.isValidSource(this.level, entry.getValue().getFirst(), entry.getKey(), consumerPosition))
				.min((o1, o2) -> {
					double distance1 = o1.getKey().distSqr(consumerPosition);
					double distance2 = o2.getKey().distSqr(consumerPosition);
					return Double.compare(distance1, distance2);
				});
		// Exit if no valid provider exists
		if (possibleProvider.isEmpty()) {
			unheatedConsumers.add(consumerPosition);
			setDirty();
			return false;
		}

		Entry<BlockPos, Pair<IHeatProvider, Set<BlockPos>>> providerEntry = possibleProvider.get();
		return addConsumerToProvider(providerEntry.getKey(), providerEntry.getValue().getFirst(), providerEntry.getValue().getSecond(), consumerPosition, consumer);
	}

	protected boolean addConsumerToProvider(BlockPos providerPos, IHeatProvider provider, Set<BlockPos> consumerSet, BlockPos consumerPos, IHeatConsumer consumer) {
		setDirty();
		if (consumerSet.add(consumerPos)) {
			consumer.onHeatProvided(this.level, provider, providerPos, consumerPos);
			return true;
		}

		return false;
	}

	public static HeatHandler load(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(tag -> load(level, tag), () -> new HeatHandler(level), KEY);
	}

	protected static HeatHandler load(Level level, CompoundTag tag) {
		return new HeatHandler(level, tag);
	}

	public void removeHeatProvider(BlockPos pos) {
		Pair<IHeatProvider, Set<BlockPos>> removedEntry = this.data.remove(pos);
		if (removedEntry == null) return;
		this.unheatedConsumers.addAll(removedEntry.getSecond());
		setDirty();
	}

	public HeatLevel getHeatFor(BlockPos consumerPos) {
		// No heat if no provider
		if (this.unheatedConsumers.contains(consumerPos)) return HeatLevel.NONE;

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
}
