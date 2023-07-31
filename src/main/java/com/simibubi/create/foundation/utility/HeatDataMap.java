package com.simibubi.create.foundation.utility;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.Create;
import com.simibubi.create.api.heat.IHeatProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HeatDataMap extends DoubleValuesHashMap<BlockPos, IHeatProvider, Set<BlockPos>> {
	private static final String ENTRY_KEY = "block_pos";
	private static final String BLOCK_POS_X = "x";
	private static final String BLOCK_POS_Y = "y";
	private static final String BLOCK_POS_Z = "z";
	private static final String CONSUMER_TAGS = "consumers";

	public ListTag serializeNBT() {
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
		});

		return list;
	}

	public void deserializeNBT(final Level level, final ListTag nbt) {
		// Ensure no data exists
		this.clear();
		// load data from tag
		nbt.forEach(entryTag -> {
			CompoundTag entryRoot = (CompoundTag) entryTag;
			BlockPos entryKey = constructBlockPos(entryRoot.getCompound(ENTRY_KEY));
			BlockState providerState = level.getBlockState(entryKey);
			// Validate Block
			if (!(providerState.getBlock() instanceof IHeatProvider heatProvider)) {
				Create.LOGGER.warn("Error on loading heat provider at {}. {} is not an instance of IHeatProvider", entryKey.toShortString(), providerState);
				return;
			}
			// Add entries
			ListTag consumerTags = entryRoot.getList(CONSUMER_TAGS, Tag.TAG_COMPOUND);
			Set<BlockPos> consumers = new HashSet<>();
			consumerTags.forEach(consumerTag -> {
				CompoundTag consumerEntry = (CompoundTag) consumerTag;
				BlockPos consumerPos = constructBlockPos(consumerEntry);
				consumers.add(consumerPos);
			});

			put(entryKey, heatProvider, consumers);
		});
	}

	private void applyBlockPos(CompoundTag tag, BlockPos pos) {
		tag.putInt(BLOCK_POS_X, pos.getX());
		tag.putInt(BLOCK_POS_Y, pos.getY());
		tag.putInt(BLOCK_POS_Z, pos.getZ());
	}

	private BlockPos constructBlockPos(CompoundTag tag) {
		return new BlockPos(tag.getInt(BLOCK_POS_X), tag.getInt(BLOCK_POS_Y), tag.getInt(BLOCK_POS_Z));
	}
}
