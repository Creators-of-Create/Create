package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Sets;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.utility.Lang;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class MaterialChecklist {

	public static final int MAX_ENTRIES_PER_PAGE = 5;

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

		for (ItemRequirement.StackRequirement stack : requirement.requiredItems) {
			if (stack.usage == ItemUseType.DAMAGE)
				putOrIncrement(damageRequired, stack.item);
			if (stack.usage == ItemUseType.CONSUME)
				putOrIncrement(required, stack.item);
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

	public ItemStack createItem() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

		CompoundTag tag = book.getOrCreateTag();
		ListTag pages = new ListTag();

		int itemsWritten = 0;
		MutableComponent textComponent;

		if (blocksNotLoaded) {
			textComponent = new TextComponent("\n" + ChatFormatting.RED);
			textComponent =
				textComponent.append(Lang.createTranslationTextComponent("materialChecklist.blocksNotLoaded"));
			pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));
		}

		List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
		Collections.sort(keys, (item1, item2) -> {
			Locale locale = Locale.ENGLISH;
			String name1 = new TranslatableComponent(item1.getDescriptionId()).getString()
				.toLowerCase(locale);
			String name2 = new TranslatableComponent(item2.getDescriptionId()).getString()
				.toLowerCase(locale);
			return name1.compareTo(name2);
		});

		textComponent = new TextComponent("");
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
				textComponent.append(new TextComponent("\n >>>").withStyle(ChatFormatting.BLUE));
				pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));
				textComponent = new TextComponent("");
			}

			itemsWritten++;
			textComponent.append(entry(new ItemStack(item), amount, true));
		}

		for (Item item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(new TextComponent("\n >>>").withStyle(ChatFormatting.DARK_GREEN));
				pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));
				textComponent = new TextComponent("");
			}

			itemsWritten++;
			textComponent.append(entry(new ItemStack(item), getRequiredAmount(item), false));
		}

		pages.add(StringTag.valueOf(Component.Serializer.toJson(textComponent)));

		tag.put("pages", pages);
		tag.putString("author", "Schematicannon");
		tag.putString("title", ChatFormatting.BLUE + "Material Checklist");
		textComponent = Lang.createTranslationTextComponent("materialChecklist")
			.setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
				.withItalic(Boolean.FALSE));
		book.getOrCreateTagElement("display")
			.putString("Name", Component.Serializer.toJson(textComponent));
		book.setTag(tag);

		return book;
	}

	public int getRequiredAmount(Item item) {
		int amount = required.getOrDefault(item, 0);
		if (damageRequired.containsKey(item))
			amount += Math.ceil(damageRequired.getInt(item) / (float) new ItemStack(item).getMaxDamage());
		return amount;
	}

	private Component entry(ItemStack item, int amount, boolean unfinished) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		MutableComponent tc = new TranslatableComponent(item.getDescriptionId());
		if (!unfinished)
			tc.append(" \u2714");
		tc.withStyle(unfinished ? ChatFormatting.BLUE : ChatFormatting.DARK_GREEN);
		return tc.append(new TextComponent("\n" + " x" + amount).withStyle(ChatFormatting.BLACK))
			.append(
				new TextComponent(" | " + stacks + "\u25A4 +" + remainder + "\n").withStyle(ChatFormatting.GRAY));
	}

}
