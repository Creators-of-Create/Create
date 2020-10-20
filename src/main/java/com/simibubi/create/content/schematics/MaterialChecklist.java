package com.simibubi.create.content.schematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Sets;
import com.simibubi.create.content.schematics.ItemRequirement.ItemUseType;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

//TODO colors get purged with current approach, proper checklist item with UI is needed
public class MaterialChecklist {

	public Map<Item, Integer> gathered;
	public Map<Item, Integer> required;
	public Map<Item, Integer> damageRequired;
	public boolean blocksNotLoaded;

	public MaterialChecklist() {
		required = new HashMap<>();
		damageRequired = new HashMap<>();
		gathered = new HashMap<>();
	}

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

	private void putOrIncrement(Map<Item, Integer> map, ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.AIR)
			return;
		if (map.containsKey(item))
			map.put(item, map.get(item) + stack.getCount());
		else
			map.put(item, stack.getCount());
	}

	public void collect(ItemStack stack) {
		Item item = stack.getItem();
		if (required.containsKey(item) || damageRequired.containsKey(item))
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

		List<Item> keys = new ArrayList<>(Sets.union(required.keySet(), damageRequired.keySet()));
		Collections.sort(keys, (item1, item2) -> {
			Locale locale = Locale.ENGLISH;
			String name1 =
				new TranslationTextComponent(item1.getTranslationKey()).getString().toLowerCase(locale);
			String name2 =
				new TranslationTextComponent(item2.getTranslationKey()).getString().toLowerCase(locale);
			return name1.compareTo(name2);
		});

		List<Item> completed = new ArrayList<>();
		for (Item item : keys) {
			int amount = getRequiredAmount(item);
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
			string.append(unfinishedEntry(new ItemStack(item), amount).getString());
		}

		for (Item item : completed) {
			if (itemsWritten == 6) {
				itemsWritten = 0;
				string.append("\"}");
				pages.add(StringNBT.of(string.toString()));
				string = new StringBuilder("{\"text\":\"");
			}

			itemsWritten++;
			string.append(gatheredEntry(new ItemStack(item), getRequiredAmount(item)).getString());
		}

		string.append("\"}");
		pages.add(StringNBT.of(string.toString()));

		tag.put("pages", pages);
		tag.putString("author", "Schematicannon");
		tag.putString("title", TextFormatting.BLUE + "Material Checklist");
		book.setTag(tag);

		return book;
	}

	public Integer getRequiredAmount(Item item) {
		int amount = required.getOrDefault(item, 0);
		if (damageRequired.containsKey(item))
			amount += Math.ceil(damageRequired.get(item) / (float) new ItemStack(item).getMaxDamage());
		return amount;
	}

	private ITextComponent gatheredEntry(ItemStack item, int amount) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		TranslationTextComponent tc = new TranslationTextComponent(item.getTranslationKey());
		return tc.append(" \\u2714\n " + "---");
		//.formatted(TextFormatting.DARK_GREEN).append(new StringTextComponent(" | "
			//+ "-" + "\\u25A4 +" + "--" + "\n").formatted(TextFormatting.GRAY));
		// return TextFormatting.DARK_GREEN + tc.getFormattedText() + " \\u2714\n x" + amount + TextFormatting.GRAY + " | "
			//	+ stacks + "\\u25A4 +" + remainder + "\n";
	}

	private ITextComponent unfinishedEntry(ItemStack item, int amount) {
		int stacks = amount / 64;
		int remainder = amount % 64;
		TranslationTextComponent tc = new TranslationTextComponent(item.getTranslationKey());
		return tc.append("\n x" + amount).formatted(TextFormatting.BLUE).append(new StringTextComponent(" | " + stacks + "\\u25A4 +" + remainder + "\n").formatted(TextFormatting.GRAY));
		// return TextFormatting.BLUE + tc.getFormattedText() + "\n x" + amount + TextFormatting.GRAY + " | " + stacks
			//	+ "\\u25A4 +" + remainder + "\n";
	}

}
