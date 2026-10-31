package com.simibubi.create.content.schematics.cannon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Sets;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.simibubi.create.foundation.utility.CreateLang;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.network.Filterable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class MaterialChecklist {

	public static final int MAX_ENTRIES_PER_PAGE = 5;
	public static final int MAX_ENTRIES_PER_CLIPBOARD_PAGE = 7;

	public Object2IntMap<Item> gathered = new Object2IntArrayMap<>();
	public Object2IntMap<Item> required = new Object2IntArrayMap<>();
	public Object2IntMap<Item> damageRequired = new Object2IntArrayMap<>();
	public Object2IntMap<Fluid> gatheredFluids = new Object2IntArrayMap<>();
	public Object2IntMap<Fluid> requiredFluids = new Object2IntArrayMap<>();
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
		for (ItemRequirement.FluidRequirement fluid : requirement.getRequiredFluids())
			requiredFluids.put(fluid.fluid(), requiredFluids.getOrDefault(fluid.fluid(), 0) + fluid.amount());
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

	public void collect(FluidStack stack) {
		Fluid fluid = stack.getFluid();
		if (!requiredFluids.containsKey(fluid))
			return;
		gatheredFluids.put(fluid, gatheredFluids.getOrDefault(fluid, 0) + stack.getAmount());
	}

	public ItemStack createWrittenBook() {
		ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

		List<Filterable<Component>> pages = new ArrayList<>();

		int itemsWritten = 0;
		MutableComponent textComponent;

		if (blocksNotLoaded) {
			textComponent = Component.literal("\n" + ChatFormatting.RED);
			textComponent = textComponent.append(CreateLang.translateDirect("materialChecklist.blocksNotLoaded"));
			pages.add(Filterable.passThrough(textComponent));
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

        textComponent = Component.empty();
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
				textComponent.append(Component.literal("\n >>>")
					.withStyle(ChatFormatting.BLUE));
				pages.add(Filterable.passThrough(textComponent));
                textComponent = Component.empty();
			}

			itemsWritten++;
			textComponent.append(entry(new ItemStack(item), amount, true, true));
		}

		List<Fluid> completedFluids = new ArrayList<>();
		for (Fluid fluid : sortedFluids()) {
			int amount = requiredFluids.getInt(fluid) - gatheredFluids.getOrDefault(fluid, 0);
			if (amount <= 0) {
				completedFluids.add(fluid);
				continue;
			}
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(Component.literal("\n >>>")
					.withStyle(ChatFormatting.BLUE));
				pages.add(Filterable.passThrough(textComponent));
				textComponent = Component.empty();
			}
			itemsWritten++;
			textComponent.append(fluidEntry(fluid, amount, true, true));
		}

		for (Item item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(Component.literal("\n >>>")
					.withStyle(ChatFormatting.DARK_GREEN));
				pages.add(Filterable.passThrough(textComponent));
                textComponent = Component.empty();
			}

			itemsWritten++;
			textComponent.append(entry(new ItemStack(item), getRequiredAmount(item), false, true));
		}

		for (Fluid fluid : completedFluids) {
			if (itemsWritten == MAX_ENTRIES_PER_PAGE) {
				itemsWritten = 0;
				textComponent.append(Component.literal("\n >>>")
					.withStyle(ChatFormatting.DARK_GREEN));
				pages.add(Filterable.passThrough(textComponent));
				textComponent = Component.empty();
			}
			itemsWritten++;
			textComponent.append(fluidEntry(fluid, requiredFluids.getInt(fluid), false, true));
		}

		pages.add(Filterable.passThrough(textComponent));

		WrittenBookContent contents = new WrittenBookContent(
				Filterable.passThrough(ChatFormatting.BLUE + "Material Checklist"),
				"Schematicannon",
				0,
				pages,
				true
		);
		book.set(DataComponents.WRITTEN_BOOK_CONTENT, contents);
		textComponent = CreateLang.translateDirect("materialChecklist")
			.setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)
				.withItalic(Boolean.FALSE));
		book.set(DataComponents.CUSTOM_NAME, textComponent);

		return book;
	}

	public ItemStack createWrittenClipboard() {
		int itemsWritten = 0;

		List<List<ClipboardEntry>> pages = new ArrayList<>();
		List<ClipboardEntry> currentPage = new ArrayList<>();

		if (blocksNotLoaded) {
			currentPage.add(new ClipboardEntry(false, CreateLang.translateDirect("materialChecklist.blocksNotLoaded")
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
				currentPage.add(new ClipboardEntry(false, Component.literal(">>>")
					.withStyle(ChatFormatting.DARK_GRAY)));
				pages.add(currentPage);
				currentPage = new ArrayList<>();
			}

			itemsWritten++;
			currentPage.add(new ClipboardEntry(false, entry(new ItemStack(item), amount, true, false))
				.displayItem(new ItemStack(item), amount));
		}

		List<Fluid> completedFluids = new ArrayList<>();
		for (Fluid fluid : sortedFluids()) {
			int amount = requiredFluids.getInt(fluid) - gatheredFluids.getOrDefault(fluid, 0);
			if (amount <= 0) {
				completedFluids.add(fluid);
				continue;
			}
			if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
				itemsWritten = 0;
				currentPage.add(new ClipboardEntry(false, Component.literal(">>>")
					.withStyle(ChatFormatting.DARK_GRAY)));
				pages.add(currentPage);
				currentPage = new ArrayList<>();
			}
			itemsWritten++;
			currentPage.add(new ClipboardEntry(false, fluidEntry(fluid, amount, true, false))
				.displayItem(new FluidStack(fluid, FluidType.BUCKET_VOLUME), amount));
		}

		for (Item item : completed) {
			if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
				itemsWritten = 0;
				currentPage.add(new ClipboardEntry(true, Component.literal(">>>")
					.withStyle(ChatFormatting.DARK_GREEN)));
				pages.add(currentPage);
				currentPage = new ArrayList<>();
			}

			itemsWritten++;
			currentPage.add(new ClipboardEntry(true, entry(new ItemStack(item), getRequiredAmount(item), false, false))
				.displayItem(new ItemStack(item), 0));
		}

		for (Fluid fluid : completedFluids) {
			if (itemsWritten == MAX_ENTRIES_PER_CLIPBOARD_PAGE) {
				itemsWritten = 0;
				currentPage.add(new ClipboardEntry(true, Component.literal(">>>")
					.withStyle(ChatFormatting.DARK_GREEN)));
				pages.add(currentPage);
				currentPage = new ArrayList<>();
			}
			itemsWritten++;
			currentPage.add(new ClipboardEntry(true,
				fluidEntry(fluid, requiredFluids.getInt(fluid), false, false))
				.displayItem(new FluidStack(fluid, FluidType.BUCKET_VOLUME), 0));
		}

		pages.add(currentPage);

		ItemStack clipboard = AllBlocks.CLIPBOARD.asStack();
		clipboard.set(AllDataComponents.CLIPBOARD_CONTENT, new ClipboardContent(ClipboardType.WRITTEN, pages, true));
		clipboard.set(DataComponents.CUSTOM_NAME, CreateLang.translateDirect("materialChecklist")
				.setStyle(Style.EMPTY.withItalic(false)));
		return clipboard;
	}

	public int getRequiredAmount(Item item) {
		int amount = required.getOrDefault(item, 0);
		if (damageRequired.containsKey(item))
			amount += (int) Math.ceil(damageRequired.getInt(item) / (float) new ItemStack(item).getMaxDamage());
		return amount;
	}

	private MutableComponent entry(ItemStack item, int amount, boolean unfinished, boolean forBook) {
		int stacks = amount / 64;
		int remainder = amount % 64;
        MutableComponent tc = Component.empty();
		tc.append(Component.translatable(item.getDescriptionId())
			.setStyle(Style.EMPTY
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(item)))));

		if (!unfinished && forBook)
			tc.append(" \u2714");
		if (!unfinished || forBook)
			tc.withStyle(unfinished ? ChatFormatting.BLUE : ChatFormatting.DARK_GREEN);
		return tc.append(Component.literal("\n" + " x" + amount)
			.withStyle(ChatFormatting.BLACK))
			.append(Component.literal(" | " + stacks + "\u25A4 +" + remainder + (forBook ? "\n" : ""))
				.withStyle(ChatFormatting.GRAY));
	}

	private List<Fluid> sortedFluids() {
		List<Fluid> fluids = new ArrayList<>(requiredFluids.keySet());
		fluids.sort((first, second) -> first.getFluidType()
			.getDescription()
			.getString()
			.compareToIgnoreCase(second.getFluidType()
				.getDescription()
				.getString()));
		return fluids;
	}

	private MutableComponent fluidEntry(Fluid fluid, int amount, boolean unfinished, boolean forBook) {
		ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
		MutableComponent name = fluid.getFluidType()
			.getDescription()
			.copy()
			.withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				fluid.getFluidType()
					.getDescription()
					.copy()
					.append("\n")
					.append(Component.literal(id.toString())
						.withStyle(ChatFormatting.DARK_GRAY)))));
		MutableComponent text = Component.empty()
			.append(name);
		if (!unfinished && forBook)
			text.append(" \u2714");
		if (!unfinished || forBook)
			text.withStyle(unfinished ? ChatFormatting.BLUE : ChatFormatting.DARK_GREEN);
		int buckets = amount / FluidType.BUCKET_VOLUME;
		int remainder = amount % FluidType.BUCKET_VOLUME;
		return text.append(Component.literal("\n x" + amount + " mB")
				.withStyle(ChatFormatting.BLACK))
			.append(Component.literal(" | " + buckets + " B +" + remainder + " mB" + (forBook ? "\n" : ""))
				.withStyle(ChatFormatting.GRAY));
	}

}
