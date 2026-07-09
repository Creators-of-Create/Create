package com.simibubi.create.foundation.utility;

import java.util.UUID;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public final class LegacyNbtUtilsBridge {
	private LegacyNbtUtilsBridge() {}

	public static Tag writeBlockPos(BlockPos pos) {
		return LongTag.valueOf(pos.asLong());
	}

	public static BlockPos readBlockPos(Tag tag) {
		if (tag instanceof NumericTag numericTag)
			return BlockPos.of(numericTag.longValue());
		if (tag instanceof CompoundTag compoundTag)
			return new BlockPos(compoundTag.getIntOr("X", 0), compoundTag.getIntOr("Y", 0), compoundTag.getIntOr("Z", 0));
		return BlockPos.ZERO;
	}

	public static IntArrayTag createUUID(UUID uuid) {
		return new IntArrayTag(UUIDUtil.uuidToIntArray(uuid));
	}

	public static void putUUID(CompoundTag tag, String key, UUID uuid) {
		tag.put(key, createUUID(uuid));
	}

	public static boolean hasUUID(CompoundTag tag, String key) {
		return tag.contains(key);
	}

	public static UUID getUUID(CompoundTag tag, String key) {
		return loadUUID(tag.get(key));
	}

	public static UUID loadUUID(Tag tag) {
		if (tag instanceof IntArrayTag intArrayTag) {
			int[] value = intArrayTag.getAsIntArray();
			if (value.length == 4)
				return UUIDUtil.uuidFromIntArray(value);
		}
		if (tag instanceof StringTag stringTag)
			return UUID.fromString(stringTag.value());
		if (tag instanceof CompoundTag compoundTag) {
			long most = compoundTag.getLongOr("M", compoundTag.getLongOr("UUIDMost", 0));
			long least = compoundTag.getLongOr("L", compoundTag.getLongOr("UUIDLeast", 0));
			if (most != 0 || least != 0)
				return new UUID(most, least);
		}
		return new UUID(0, 0);
	}

	public static ListTag saveInventory(Inventory inventory, HolderLookup.Provider registries, String key) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		inventory.save(output.list(key, ItemStackWithSlot.CODEC));
		return output.buildResult().getListOrEmpty(key);
	}

	public static void loadInventory(Inventory inventory, HolderLookup.Provider registries, CompoundTag tag, String key) {
		inventory.load(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag)
			.listOrEmpty(key, ItemStackWithSlot.CODEC));
	}
}
