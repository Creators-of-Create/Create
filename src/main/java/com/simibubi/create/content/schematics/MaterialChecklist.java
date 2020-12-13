package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Sets;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.utility.Lang;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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

		for (ItemStack stack : requirement.requiredItems) {
			if (requirement.getUsage() == ItemUseType.DAMAGE)
				putOrIncrement(damageRequired, stack);
			if (requirement.getUsage() == ItemUseType.CONSUME)
				putOrIncrement(required, stack);
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

		CompoundNBT tag = book.getOrCreateTag();
		ListNBT pages = new ListNBT();

		int itemsWritten = 0;
		ITextComponent textComponent;

		if (blocksNotLoaded) {
			textComponent = new StringTextComponent("\n" + TextFormatting.RED);
			textComponent =
				textComponent.appendSibling(Lang.createTranslationTextComponent("materialChecklist.blocksNotLoaded"));
			pages.add(StringNBT.of(ITextComponent.Serializer.toJson(textComponent)));
		}

		List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
		Collections.sort(keys, (item1, item2) -> {
			String name1 = item1.getRegistryName()
				.getPath();
			String name2 = item2.getRegistryName()
				.getPath();
			return name1.compareTo(name2);
		});

		textComponent = new StringTextComponent("");
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
				textComponent =
					textComponent.appendSibling(new StringTextComponent("\n >>>").applyTextStyle(TextFormatting.BLUE));
				pages.add(StringNBT.of(ITextComponent.Serializer.toJson(textComponent)));
				textComponent = new StringTextComponent("");
			}

			itemsWritten++;
			textComponent.appendSibling(entry(new ItemStack(item), amount, true));
		}

		for (Item item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent =
					textComponent.appendSibling(new StringTextComponent("\n >>>").applyTextStyle(TextFormatting.DARK_GREEN));
				pages.add(StringNBT.of(ITextComponent.Serializer.toJson(textComponent)));
				textComponent = new StringTextComponent("");
			}

			itemsWritten++;
			textComponent.appendSibling(entry(new ItemStack(item), getRequiredAmount(item), false));
		}

		pages.add(StringNBT.of(ITextComponent.Serializer.toJson(textComponent)));

		tag.put("pages", pages);
		tag.putString("author", "Schematicannon");
		tag.putString("title", TextFormatting.BLUE + "Material Checklist");
		textComponent = Lang.createTranslationTextComponent("materialChecklist")
			.setStyle(new Style().setColor(TextFormatting.BLUE)
				.setItalic(Boolean.FALSE));
		book.getOrCreateChildTag("display")
			.putString("Name", ITextComponent.Serializer.toJson(textComponent));
		book.setTag(tag);

		return book;
	}

	public int getRequiredAmount(Item item) {
		int amount = required.getOrDefault(item, 0);
		if (damageRequired.containsKey(item))
			amount += Math.ceil(damageRequired.getInt(item) / (float) new ItemStack(item).getMaxDamage());
		return amount;
	}

	private ITextComponent entry(ItemStack item, int amount, boolean unfinished) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		ITextComponent tc = new TranslationTextComponent(item.getTranslationKey());
		if (!unfinished)
			tc.appendText(" \u2714");
		tc.applyTextStyle(unfinished ? TextFormatting.BLUE : TextFormatting.DARK_GREEN);
		return tc.appendText("\n" + TextFormatting.BLACK + " x" + amount + TextFormatting.GRAY + " | " + stacks
			+ "\u25A4 +" + remainder + "\n");
	}

}
