package com.simibubi.create.foundation.utility;

import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.simibubi.create.foundation.item.SmartInventory;

public final class LegacyItemStackNbtBridge {
	private LegacyItemStackNbtBridge() {}

	public static Tag saveOptional(ItemStack stack, HolderLookup.Provider registries) {
		return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
			.result()
			.orElseGet(CompoundTag::new);
	}

	public static CompoundTag saveOptionalCompound(ItemStack stack, HolderLookup.Provider registries) {
		Tag tag = saveOptional(stack, registries);
		return tag instanceof CompoundTag compoundTag ? compoundTag : new CompoundTag();
	}

	public static Optional<ItemStack> parse(HolderLookup.Provider registries, Tag tag) {
		return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
			.result();
	}

	public static Optional<ItemStack> parse(HolderLookup.Provider registries, Optional<? extends Tag> tag) {
		return tag.flatMap(t -> parse(registries, t));
	}

	public static ItemStack parseOptional(HolderLookup.Provider registries, Tag tag) {
		return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
			.result()
			.orElse(ItemStack.EMPTY);
	}

	public static ItemStack parseOptional(HolderLookup.Provider registries, Optional<? extends Tag> tag) {
		return tag.map(t -> parseOptional(registries, t))
			.orElse(ItemStack.EMPTY);
	}

	public static CompoundTag serializeHandler(ItemStackHandler handler, HolderLookup.Provider registries) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		handler.serialize(output);
		return output.buildResult();
	}

	public static void deserializeHandler(ItemStackHandler handler, HolderLookup.Provider registries, CompoundTag tag) {
		handler.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, tag));
	}

	public static CompoundTag serializeHandler(SmartInventory handler, HolderLookup.Provider registries) {
		return handler.serializeNBT(registries);
	}

	public static void deserializeHandler(SmartInventory handler, HolderLookup.Provider registries, CompoundTag tag) {
		handler.deserializeNBT(registries, tag);
	}
}
