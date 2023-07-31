package com.simibubi.create.foundation.heat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.logging.log4j.util.TriConsumer;

import com.simibubi.create.Create;
import com.simibubi.create.api.heat.HeatConsumer;
import com.simibubi.create.api.heat.HeatProviders;
import com.simibubi.create.api.heat.HeatProviders.HeatProvider;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HeatDataMap {
	private static final String ENTRY_KEY = "block_pos";
	private static final String BLOCK_POS_X = "x";
	private static final String BLOCK_POS_Y = "y";
	private static final String BLOCK_POS_Z = "z";
	private static final String CONSUMER_TAGS = "consumers";
	private static final String UNHEATED_CONSUMERS = "unheated";
	private static final String HEAT_MAP = "heated";
	private final Set<BlockPos> unheatedConsumers = new HashSet<>();
	private final Map<BlockPos, Pair<HeatProvider, Set<BlockPos>>> heatProviders = new HashMap<>();

	public CompoundTag serializeNBT() {
		CompoundTag rootTag = new CompoundTag();
		rootTag.put(HEAT_MAP, serializeHeatMap());
		rootTag.put(UNHEATED_CONSUMERS, serializeUnheatedSet());
		return rootTag;
	}

	private ListTag serializeUnheatedSet() {
		ListTag list = new ListTag();

		unheatedConsumers.forEach(pos -> {
			CompoundTag consumerPosTag = new CompoundTag();
			applyBlockPos(consumerPosTag, pos);
			list.add(consumerPosTag);
		});

		return list;
	}

	private ListTag serializeHeatMap() {
		ListTag list = new ListTag();

		forEach((providerPos, provider, consumers) -> {
			CompoundTag entryRoot = new CompoundTag();

			CompoundTag entryKey = new CompoundTag();
			applyBlockPos(entryKey, providerPos);
			entryRoot.put(ENTRY_KEY, entryKey);

			ListTag consumerTags = new ListTag();
			consumers.forEach(consumerPos -> {
				CompoundTag consumerPosTag = new CompoundTag();
				applyBlockPos(consumerPosTag, consumerPos);
				consumerTags.add(consumerPosTag);
			});
			entryRoot.put(CONSUMER_TAGS, consumerTags);
			list.add(entryRoot);
		});

		return list;
	}

	public void deserializeNBT(final Level level, final CompoundTag nbt) {
		deserializeHeatMap(level, nbt.getList(HEAT_MAP, Tag.TAG_COMPOUND));
		deserializeUnheatedSet(level, nbt.getList(UNHEATED_CONSUMERS, Tag.TAG_COMPOUND));
	}

	private void deserializeUnheatedSet(final Level level, final ListTag tag) {
		this.unheatedConsumers.clear();
		tag.forEach(t -> getSaveConsumerPos(level, t).ifPresent(unheatedConsumers::add));
	}

	private void deserializeHeatMap(final Level level, final ListTag tag) {
		// Ensure no data exists
		this.heatProviders.clear();
		// load data from tag
		tag.forEach(entryTag -> {
			CompoundTag entryRoot = (CompoundTag) entryTag;
			BlockPos entryKey = constructBlockPos(entryRoot.getCompound(ENTRY_KEY));
			BlockState providerState = level.getBlockState(entryKey);
			// Validate Block
			if (!HeatProviders.isHeatProvider(providerState)) {
				Create.LOGGER.warn("Error on loading heat provider at {}. {} is not an instance of IHeatProvider", entryKey.toShortString(), providerState);
				return;
			}
			// Add entries
			ListTag consumerTags = entryRoot.getList(CONSUMER_TAGS, Tag.TAG_COMPOUND);
			Set<BlockPos> consumers = new HashSet<>();
			consumerTags.forEach(t -> getSaveConsumerPos(level, t).ifPresent(consumers::add));
			storeProvider(entryKey, HeatProviders.getHeatProviderOf(providerState), consumers);
		});
	}

	private Optional<BlockPos> getSaveConsumerPos(Level level, Tag tag) {
		CompoundTag consumerTag = (CompoundTag) tag;
		BlockPos consumerPos = constructBlockPos(consumerTag);
		if (!HeatConsumer.isValidConsumer(level, consumerPos)) {
			Create.LOGGER.warn("Error on loading heat consumer at {}. Invalid Block.", consumerPos);
			return Optional.empty();
		}
		return Optional.of(consumerPos);
	}

	public Set<BlockPos> getUnheatedConsumers() {
		return unheatedConsumers;
	}

	private void applyBlockPos(CompoundTag tag, BlockPos pos) {
		tag.putInt(BLOCK_POS_X, pos.getX());
		tag.putInt(BLOCK_POS_Y, pos.getY());
		tag.putInt(BLOCK_POS_Z, pos.getZ());
	}

	private BlockPos constructBlockPos(CompoundTag tag) {
		return new BlockPos(tag.getInt(BLOCK_POS_X), tag.getInt(BLOCK_POS_Y), tag.getInt(BLOCK_POS_Z));
	}

	public void forEach(TriConsumer<BlockPos, HeatProvider, Set<BlockPos>> action) {
		this.heatProviders.forEach((k, v1V2Pair) -> action.accept(k, v1V2Pair.getFirst(), v1V2Pair.getSecond()));
	}

	@Nullable
	public Pair<HeatProvider, Set<BlockPos>> storeProvider(BlockPos key, HeatProvider value1, Set<BlockPos> value2) {
		return this.heatProviders.put(key, Pair.of(value1, value2));
	}

	public boolean containsProviderAt(BlockPos pos) {
		return this.heatProviders.containsKey(pos);
	}

	public Set<Entry<BlockPos, Pair<HeatProvider, Set<BlockPos>>>> getActiveHeatProviders() {
		return this.heatProviders.entrySet();
	}

	public Pair<HeatProvider, Set<BlockPos>> removeHeatProvider(BlockPos pos) {
		return this.heatProviders.remove(pos);
	}
}
