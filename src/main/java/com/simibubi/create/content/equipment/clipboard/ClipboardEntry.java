package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class ClipboardEntry {
	public static final Codec<ClipboardEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.BOOL.fieldOf("checked").forGetter(c -> c.checked),
			ComponentSerialization.CODEC.fieldOf("text").forGetter(c -> c.text),
			ItemStack.OPTIONAL_CODEC.fieldOf("icon").forGetter(c -> c.icon),
			Codec.INT.fieldOf("item_amount").forGetter(c -> c.itemAmount)
	).apply(i, (checked, text, icon, itemAmount) -> {
		ClipboardEntry entry = new ClipboardEntry(checked, text.copy());
		if (!icon.isEmpty())
			entry.displayItem(icon, itemAmount);

		return entry;
	}));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardEntry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, c -> c.checked,
			ComponentSerialization.STREAM_CODEC, c -> c.text,
			ItemStack.OPTIONAL_STREAM_CODEC, c -> c.icon,
			ByteBufCodecs.INT, c -> c.itemAmount,
			(checked, text, icon, itemAmount) -> {
				ClipboardEntry entry = new ClipboardEntry(checked, text.copy());
				if (!icon.isEmpty())
					entry.displayItem(icon, itemAmount);

				return entry;
			}
	);

	public boolean checked;
	public MutableComponent text;
	public ItemStack icon;
	public int itemAmount;

	public ClipboardEntry(boolean checked, MutableComponent text) {
		this.checked = checked;
		this.text = text;
		this.icon = ItemStack.EMPTY;
	}

	public ClipboardEntry displayItem(ItemStack icon, int amount) {
		this.icon = icon;
		this.itemAmount = amount;
		return this;
	}

	public static List<List<ClipboardEntry>> readAll(ItemStack clipboardItem) {
		List<List<ClipboardEntry>> entries = new ArrayList<>();

		// Both these lists are immutable, so we unfortunately need to re-create them to make them mutable
		List<List<ClipboardEntry>> saved = clipboardItem.getOrDefault(AllDataComponents.CLIPBOARD_PAGES, Collections.emptyList());
		for (List<ClipboardEntry> inner : saved)
			entries.add(new ArrayList<>(inner));

		return entries;
	}

	public static List<ClipboardEntry> getLastViewedEntries(ItemStack heldItem) {
		List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(heldItem);
		if (pages.isEmpty())
			return new ArrayList<>();
		int page = !heldItem.has(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE) ? 0
			: Math.min(heldItem.getOrDefault(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE, 0), pages.size() - 1);
		List<ClipboardEntry> entries = pages.get(page);
		return entries;
	}

	public static void saveAll(List<List<ClipboardEntry>> entries, ItemStack clipboardItem) {
		clipboardItem.set(AllDataComponents.CLIPBOARD_PAGES, entries);
	}

	public CompoundTag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("Checked", checked);
		nbt.putString("Text", Component.Serializer.toJson(text, RegistryAccess.EMPTY));
		if (icon.isEmpty())
			return nbt;
		nbt.put("Icon", icon.saveOptional(RegistryAccess.EMPTY));
		nbt.putInt("ItemAmount", itemAmount);
		return nbt;
	}

	public static ClipboardEntry readNBT(CompoundTag tag) {
		ClipboardEntry clipboardEntry =
			new ClipboardEntry(tag.getBoolean("Checked"), Component.Serializer.fromJson(tag.getString("Text"), RegistryAccess.EMPTY));
		if (tag.contains("Icon"))
			clipboardEntry.displayItem(ItemStack.parseOptional(RegistryAccess.EMPTY, tag.getCompound("Icon")), tag.getInt("ItemAmount"));
		return clipboardEntry;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClipboardEntry that)) return false;

		return checked == that.checked && text.equals(that.text) && ItemStack.isSameItemSameComponents(icon, that.icon);
	}

	@Override
	public int hashCode() {
		int result = Boolean.hashCode(checked);
		result = 31 * result + text.hashCode();
		result = 31 * result + ItemStack.hashItemAndComponents(icon);
		return result;
	}
}
