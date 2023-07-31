package com.simibubi.create.api.heat;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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
	}

	/**
	 * Adds a heat consumer to the nearest valid heat provider.
	 *
	 * @return true if successfully added
	 */
	public boolean addHeatConsumer(BlockPos consumer) {
		return addHeatConsumer(consumer, provider -> true);
	}

	/**
	 * Adds a heat consumer to the nearest valid heat provider.
	 *
	 * @return true if successfully added
	 */
	public boolean addHeatConsumer(BlockPos consumerPosition, Predicate<IHeatProvider> providerValidator) {
		// Get the closest possible heat provider
		Optional<Entry<BlockPos, Pair<IHeatProvider, Set<BlockPos>>>> possibleProvider = this.data.entrySet()
				.stream()
				.filter(entry -> entry.getValue().getFirst().isInHeatRange(this.level, entry.getKey(), consumerPosition))
				.filter(entry -> providerValidator.test(entry.getValue().getFirst()))
				.min((o1, o2) -> {
					double distance1 = o1.getKey().distSqr(consumerPosition);
					double distance2 = o2.getKey().distSqr(consumerPosition);
					return Double.compare(distance1, distance2);
				});
		// Exit if no valid provider exists
		if (possibleProvider.isEmpty()) return false;

		Entry<BlockPos, Pair<IHeatProvider, Set<BlockPos>>> providerEntry = possibleProvider.get();
		Set<BlockPos> consumerSet = providerEntry.getValue().getSecond();

		BlockState consumerState = this.level.getBlockState(consumerPosition);
		if (!(consumerState.getBlock() instanceof IHeatConsumer consumer)) return false;
		boolean successfulAdded = consumerSet.add(consumerPosition);

		if (successfulAdded) {
			// Callback to block when added successfully
			consumer.onHeatProvided(this.level, providerEntry.getValue().getFirst(), providerEntry.getKey(), consumerPosition);
		}

		return successfulAdded;
	}

	public static HeatHandler load(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(tag -> load(level, tag), () -> new HeatHandler(level), KEY);
	}

	protected static HeatHandler load(Level level, CompoundTag tag) {
		return new HeatHandler(level, tag);
	}
}
