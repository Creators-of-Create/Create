package com.simibubi.create.content.schematics.cannon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Sets;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MaterialChecklist {

	public static final int MAX_ENTRIES_PER_PAGE = 5;
	public static final int MAX_ENTRIES_PER_CLIPBOARD_PAGE = 7;

	public Object2IntMap<Item> gathered = new Object2IntArrayMap<>();
	public Object2IntMap<Item> required = new Object2IntArrayMap<>();
	public Object2IntMap<Item> damageRequired = new Object2IntArrayMap<>();
	public boolean blocksNotLoaded;

	public void warnBlockNotLoaded() {
		blocksNotLoaded = true;
	}

	public void require(ItemRequirement requirement) {
		if (requirement.isEmpty())
			return;
		if (requirement.isInvalid())
			return;

		for (ItemRequirement.StackRequirement stack : requirement.getRequiredItems()) {
			if (stack.usage == ItemUseType.DAMAGE)
				putOrIncrement(damageRequired, stack.stack);
			if (stack.usage == ItemUseType.CONSUME)
				putOrIncrement(required, stack.stack);
		}
	}

	private void putOrIncrement(Object2IntMap<Item> map, ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.AIR)
			return;
		if (map.containsKey(item))
			map.put(item, map.getInt(item) + stack.getCount());
		else
			map.put(item, stack.getCount());
	}

	public void collect(ItemStack stack) {
		Item item = stack.getItem();
		if (required.containsKey(item) || damageRequired.containsKey(item))
			if (gathered.containsKey(item))
				gathered.put(item, gathered.getInt(item) + stack.getCount());
			else
				gathered.put(item, stack.getCount());
	}

	public ItemStack createWrittenBook() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

		CompoundTag tag = book.getOrCreateTag();
		ListTag pages = new ListTag();

		int itemsWritten = 0;
		MutableComponent textComponent;

		if (blocksNotLoaded) {
			textComponent = Components.literal("\n" + ChatFormatting.RED);
			textComponent = textComponent.append(Lang.translateDirect("materialChecklist.blocksNotLoaded"));
			pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));
		}

		List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
		Collections.sort(keys, (item1, item2) -> {
			Locale locale = Locale.ENGLISH;
			String name1 = item1.getDescription()
				.getString()
				.toLowerCase(locale);
			String name2 = item2.getDescription()
				.getString()
				.toLowerCase(locale);
			return name1.compareTo(name2);
		});

		textComponent = Components.empty();
		List<Item> completed = new ArrayList<>();
		for (Item item : keys) {
			int amount = getRequiredAmount(item);
			if (gathered.containsKey(item))
				amount -= gathered.getInt(item);

			if (amount <= 0) {
				completed.add(item);
				continue;
			}

			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(Components.literal("\n >>>")
					.withStyle(ChatFormatting.BLUE));
				pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));
				textComponent = Components.empty();
			}

			itemsWritten++;
			textComponent.append(entry(new ItemStack(item), amount, true, true));
		}

		for (Item item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(Components.literal("\n >>>")
					.withStyle(ChatFormatting.DARK_GREEN));
				pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));
				textComponent = Components.empty();
			}

			itemsWritten++;
			textComponent.append(entry(new ItemStack(item), getRequiredAmount(item), false, true));
		}

		pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));

		tag.put("pages", pages);
		tag.putBoolean("readonly", true);
		tag.putString("author", "Schematicannon");
		tag.putString("title", ChatFormatting.BLUE + "Material Checklist");
		textComponent = Lang.translateDirect("materialChecklist")
			.setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
				.withItalic(Boolean.FALSE));
		book.getOrCreateTagElement("display")
			.putString("Name", Component.Serializer.toJson(textComponent));
		book.setTag(tag);

		return book;
	}

	public ItemStack createWrittenClipboard() {
		ItemStack clipboard = AllBlocks.CLIPBOARD.asStack();
		CompoundTag tag = clipboard.getOrCreateTag();
		int itemsWritten = 0;

		List<List<ClipboardEntry>> pages = new ArrayList<>();
		List<ClipboardEntry> currentPage = new ArrayList<>();

		if (blocksNotLoaded) {
			currentPage.add(new ClipboardEntry(false, Lang.translateDirect("materialChecklist.blocksNotLoaded")
				.withStyle(ChatFormatting.RED)));
		}

		List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
		Collections.sort(keys, (item1, item2) -> {
			Locale locale = Locale.ENGLISH;
			String name1 = item1.getDescription()
				.getString()
				.toLowerCase(locale);
			String name2 = item2.getDescription()
				.getString()
				.toLowerCase(locale);
			return name1.compareTo(name2);
		});

		List<Item> completed = new ArrayList<>();
		for (Item item : keys) {
			int amount = getRequiredAmount(item);
			if (gathered.containsKey(item))
				amount -= gathered.getInt(item);

			if (amount <= 0) {
				completed.add(item);
				continue;
			}

			if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
				itemsWritten = 0;
				currentPage.add(new ClipboardEntry(false, Components.literal(">>>")
					.withStyle(ChatFormatting.DARK_GRAY)));
				pages.add(currentPage);
				currentPage = new ArrayList<>();
			}

			itemsWritten++;
			currentPage.add(new ClipboardEntry(false, entry(new ItemStack(item), amount, true, false))
				.displayItem(new ItemStack(item)));
		}

		for (Item item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
				itemsWritten = 0;
				currentPage.add(new ClipboardEntry(true, Components.literal(">>>")
					.withStyle(ChatFormatting.DARK_GREEN)));
				pages.add(currentPage);
				currentPage = new ArrayList<>();
			}

			itemsWritten++;
			currentPage.add(new ClipboardEntry(true, entry(new ItemStack(item), getRequiredAmount(item), false, false))
				.displayItem(new ItemStack(item)));
		}

		pages.add(currentPage);
		ClipboardEntry.saveAll(pages, clipboard);
		ClipboardOverrides.switchTo(ClipboardType.WRITTEN, clipboard);
		clipboard.getOrCreateTagElement("display")
			.putString("Name", Component.Serializer.toJson(Lang.translateDirect("materialChecklist")
				.setStyle(Style.EMPTY.withItalic(Boolean.FALSE))));
		tag.putBoolean("Readonly", true);
		clipboard.setTag(tag);
		return clipboard;
	}

	public int getRequiredAmount(Item item) {
		int amount = required.getOrDefault(item, 0);
		if (damageRequired.containsKey(item))
			amount += Math.ceil(damageRequired.getInt(item) / (float) new ItemStack(item).getMaxDamage());
		return amount;
	}

	private MutableComponent entry(ItemStack item, int amount, boolean unfinished, boolean forBook) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		MutableComponent tc = Components.empty();
		tc.append(Components.translatable(item.getDescriptionId())
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item)))));

		if (!unfinished && forBook)
			tc.append(" \u2714");
		if (!unfinished || forBook)
			tc.withStyle(unfinished ? ChatFormatting.BLUE : ChatFormatting.DARK_GREEN);
		return tc.append(Components.literal("\n" + " x" + amount)
			.withStyle(ChatFormatting.BLACK))
			.append(Components.literal(" | " + stacks + "\u25A4 +" + remainder + (forBook ? "\n" : ""))
				.withStyle(ChatFormatting.GRAY));
	}

}
