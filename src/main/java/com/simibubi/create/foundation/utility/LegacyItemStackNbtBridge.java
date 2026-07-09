package com.simibubi.create.foundation.utility;

import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
		return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), normalizeLegacyItemStack(tag))
			.result();
	}

	public static Optional<ItemStack> parse(HolderLookup.Provider registries, Optional<? extends Tag> tag) {
		return tag.flatMap(t -> parse(registries, t));
	}

	public static ItemStack parseOptional(HolderLookup.Provider registries, Tag tag) {
		return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), normalizeLegacyItemStack(tag))
			.result()
			.orElse(ItemStack.EMPTY);
	}

	private static Tag normalizeLegacyItemStack(Tag tag) {
		if (!(tag instanceof CompoundTag compound))
			return tag;
		if (!compound.contains("Count") && !compound.contains("tag"))
			return tag;

		CompoundTag normalized = compound.copy();
		if (normalized.contains("Count") && !normalized.contains("count"))
			normalized.putInt("count", normalized.getIntOr("Count", 1));

		if (normalized.contains("tag")) {
			CompoundTag legacyData = normalized.getCompoundOrEmpty("tag");
			CompoundTag components = normalized.getCompoundOrEmpty("components");
			migrateLegacyCreateItemComponents(normalized.getStringOr("id", ""), legacyData, components);
			if (!components.isEmpty())
				normalized.put("components", components);
			normalized.remove("tag");
		}

		normalized.remove("Count");
		return normalized;
	}

	private static void migrateLegacyCreateItemComponents(String itemId, CompoundTag legacyData, CompoundTag components) {
		if ("create:filter".equals(itemId)) {
			migrateLegacyFilterItems(legacyData, components);
			if (legacyData.contains("RespectNBT"))
				components.putBoolean("create:filter_items_respect_nbt", legacyData.getBooleanOr("RespectNBT", false));
			if (legacyData.contains("Blacklist"))
				components.putBoolean("create:filter_items_blacklist", legacyData.getBooleanOr("Blacklist", false));
			return;
		}

		if ("create:attribute_filter".equals(itemId) && legacyData.contains("WhitelistMode")) {
			String[] modes = { "whitelist_disj", "whitelist_conj", "blacklist" };
			int mode = Math.clamp(legacyData.getIntOr("WhitelistMode", 0), 0, modes.length - 1);
			components.putString("create:attribute_filter_whitelist_mode", modes[mode]);
		}
	}

	private static void migrateLegacyFilterItems(CompoundTag legacyData, CompoundTag components) {
		CompoundTag handler = legacyData.getCompoundOrEmpty("Items");
		ListTag legacyItems = handler.getListOrEmpty("Items");
		if (legacyItems.isEmpty())
			return;

		ListTag contents = new ListTag();
		for (Tag legacyItemTag : legacyItems) {
			if (!(legacyItemTag instanceof CompoundTag legacyItem))
				continue;

			CompoundTag item = ((CompoundTag) normalizeLegacyItemStack(legacyItem)).copy();
			item.remove("Slot");
			if (item.isEmpty())
				continue;

			CompoundTag entry = new CompoundTag();
			entry.putInt("slot", legacyItem.getIntOr("Slot", 0));
			entry.put("item", item);
			contents.add(entry);
		}

		if (!contents.isEmpty())
			components.put("create:filter_items", contents);
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
