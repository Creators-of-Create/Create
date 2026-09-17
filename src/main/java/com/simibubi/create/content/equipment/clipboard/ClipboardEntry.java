package com.simibubi.create.content.equipment.clipboard;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
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

import net.neoforged.neoforge.fluids.FluidStack;

public class ClipboardEntry {
	public static final Codec<ClipboardEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.BOOL.fieldOf("checked").forGetter(c -> c.checked),
			ComponentSerialization.CODEC.fieldOf("text").forGetter(c -> c.text),
			Codec.either(ItemStack.OPTIONAL_CODEC, FluidStack.OPTIONAL_CODEC).fieldOf("icon").forGetter(c -> c.icon),
			Codec.INT.fieldOf("item_amount").forGetter(c -> c.itemAmount)
	).apply(i, (checked, text, icon, itemAmount) -> {
		ClipboardEntry entry = new ClipboardEntry(checked, text.copy());
		entry.icon = icon;
		entry.itemAmount = itemAmount;

		return entry;
	}));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClipboardEntry> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, c -> c.checked,
			ComponentSerialization.STREAM_CODEC, c -> c.text,
			ByteBufCodecs.either(ItemStack.OPTIONAL_STREAM_CODEC, FluidStack.OPTIONAL_STREAM_CODEC), c -> c.icon,
			ByteBufCodecs.INT, c -> c.itemAmount,
			(checked, text, icon, itemAmount) -> {
				ClipboardEntry entry = new ClipboardEntry(checked, text.copy());
				entry.icon = icon;
				entry.itemAmount = itemAmount;

				return entry;
			}
	);

	public boolean checked;
	public MutableComponent text;
	public Either<ItemStack, FluidStack> icon;
	public int itemAmount;

	public ClipboardEntry(boolean checked, MutableComponent text) {
		this.checked = checked;
		this.text = text;
		this.icon = Either.left(ItemStack.EMPTY);
	}

	public ClipboardEntry displayItem(ItemStack icon, int amount) {
		this.icon = Either.left(icon);
		this.itemAmount = amount;
		return this;
	}

	public ClipboardEntry displayItem(FluidStack icon, int amount) {
		this.icon = Either.right(icon);
		this.itemAmount = amount;
		return this;
	}

	public boolean isIconEmpty() {
		return icon.map(ItemStack::isEmpty, FluidStack::isEmpty);
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

		if (checked != that.checked || itemAmount != that.itemAmount || !text.equals(that.text))
			return false;
		return icon.map(
			stack -> that.icon.map(other -> ItemStack.isSameItemSameComponents(stack, other), fluid -> false),
			fluid -> that.icon.map(stack -> false, other -> FluidStack.isSameFluidSameComponents(fluid, other)));
	}

	@Override
	public int hashCode() {
		int result = Boolean.hashCode(checked);
		result = 31 * result + text.hashCode();
		result = 31 * result + icon.map(ItemStack::hashItemAndComponents, FluidStack::hashFluidAndComponents);
		result = 31 * result + itemAmount;
		return result;
	}
}
