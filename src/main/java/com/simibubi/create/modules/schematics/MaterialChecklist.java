package com.simibubi.create.modules.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class MaterialChecklist {

	public Map<Item, Integer> gathered;
	public Map<Item, Integer> required;
	public boolean blocksNotLoaded;

	public MaterialChecklist() {
		required = new HashMap<>();
		gathered = new HashMap<>();
	}

	public void warnBlockNotLoaded() {
		blocksNotLoaded = true;
	}

	public void require(Item item) {
		if (required.containsKey(item))
			required.put(item, required.get(item) + 1);
		else
			required.put(item, 1);
	}

	public void collect(ItemStack stack) {
		Item item = stack.getItem();
		if (required.containsKey(item))
			if (gathered.containsKey(item))
				gathered.put(item, gathered.get(item) + stack.getCount());
			else
				gathered.put(item, stack.getCount());
	}

	public ItemStack createItem() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

		CompoundNBT tag = book.getOrCreateTag();
		ListNBT pages = new ListNBT();

		int itemsWritten = 0;
		StringBuilder string = new StringBuilder("{\"text\":\"");

		if (blocksNotLoaded) {
			string.append("\n" + TextFormatting.RED + "* Disclaimer *\n\n");
			string.append("Material List may be inaccurate due to relevant chunks not being loaded.");
			string.append("\"}");
			pages.add(StringNBT.of(string.toString()));
			string = new StringBuilder("{\"text\":\"");
		}

		List<Item> keys = new ArrayList<>(required.keySet());
		Collections.sort(keys, (item1, item2) -> {
			Locale locale = Locale.ENGLISH;
			String name1 = new TranslationTextComponent(((Item) item1).getTranslationKey()).getFormattedText()
					.toLowerCase(locale);
			String name2 = new TranslationTextComponent(((Item) item2).getTranslationKey()).getFormattedText()
					.toLowerCase(locale);
			return name1.compareTo(name2);
		});

		List<Item> completed = new ArrayList<>();
		for (Item item : keys) {
			int amount = required.get(item);
			if (gathered.containsKey(item))
				amount -= gathered.get(item);

			if (amount <= 0) {
				completed.add(item);
				continue;
			}

			if (itemsWritten == 6) {
				itemsWritten = 0;
				string.append("\"}");
				pages.add(StringNBT.of(string.toString()));
				string = new StringBuilder("{\"text\":\"");
			}

			itemsWritten++;
			string.append(unfinishedEntry(new ItemStack(item), amount));
		}

		for (Item item : completed) {
			if (itemsWritten == 6) {
				itemsWritten = 0;
				string.append("\"}");
				pages.add(StringNBT.of(string.toString()));
				string = new StringBuilder("{\"text\":\"");
			}

			itemsWritten++;
			string.append(gatheredEntry(new ItemStack(item), required.get(item)));
		}

		string.append("\"}");
		pages.add(StringNBT.of(string.toString()));

		tag.put("pages", pages);
		tag.putString("author", "Schematicannon");
		tag.putString("title", TextFormatting.BLUE + "Material Checklist");
		book.setTag(tag);

		return book;
	}

	private String gatheredEntry(ItemStack item, int amount) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		ITextComponent tc = new TranslationTextComponent(item.getTranslationKey());
		return TextFormatting.DARK_GREEN + tc.getFormattedText() + " \\u2714\n x" + amount + TextFormatting.GRAY + " | "
				+ stacks + "\\u25A4 +" + remainder + "\n";
	}

	private String unfinishedEntry(ItemStack item, int amount) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		ITextComponent tc = new TranslationTextComponent(item.getTranslationKey());
		return TextFormatting.BLUE + tc.getFormattedText() + "\n x" + amount + TextFormatting.GRAY + " | " + stacks
				+ "\\u25A4 +" + remainder + "\n";
	}

}
