package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllDataComponents;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
		return readAll(clipboardItem.getComponents());
	}

	public static List<List<ClipboardEntry>> readAll(DataComponentMap components) {
		return readAll(components.get(AllDataComponents.CLIPBOARD_CONTENT));
	}

	public static List<List<ClipboardEntry>> readAll(@Nullable ClipboardContent content) {
		if (content == null)
			return new ArrayList<>();

		// Both these lists are immutable, so we unfortunately need to re-create them to make them mutable
		List<List<ClipboardEntry>> saved = content.pages();

		List<List<ClipboardEntry>> entries = new ArrayList<>(saved.size());
		for (List<ClipboardEntry> inner : saved)
			entries.add(new ArrayList<>(inner));

		return entries;
	}

	public static List<ClipboardEntry> getLastViewedEntries(ItemStack heldItem) {
		List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(heldItem);
		if (pages.isEmpty())
			return new ArrayList<>();

		int previouslyOpenedPage = heldItem.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY).previouslyOpenedPage();
		int page = Math.min(previouslyOpenedPage, pages.size() - 1);
		return pages.get(page);
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
