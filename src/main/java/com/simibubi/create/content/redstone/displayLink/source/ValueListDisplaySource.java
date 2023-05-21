package com.simibubi.create.content.redstone.displayLink.source;

import static com.simibubi.create.content.trains.display.FlapDisplaySection.MONOSPACE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ValueListDisplaySource extends DisplaySource {

	protected abstract Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows);

	protected abstract boolean valueFirst();

	@Override
	public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
		boolean isBook = context.getTargetBlockEntity() instanceof LecternBlockEntity;

		List<MutableComponent> list = provideEntries(context, stats.maxRows() * (isBook ? ENTRIES_PER_PAGE : 1))
			.map(e -> createComponentsFromEntry(context, e))
			.map(l -> {
				MutableComponent combined = l.get(0)
					.append(l.get(1));
				if (l.size() > 2)
					combined.append(l.get(2));
				return combined;
			})
			.toList();

		if (isBook)
			list = condensePages(list);

		return list;
	}

	static final int ENTRIES_PER_PAGE = 8;

	private List<MutableComponent> condensePages(List<MutableComponent> list) {
		List<MutableComponent> condensed = new ArrayList<>();
		MutableComponent current = null;
		for (int i = 0; i < list.size(); i++) {
			MutableComponent atIndex = list.get(i);
			if (current == null) {
				current = atIndex;
				continue;
			}
			current.append(Components.literal("\n"))
				.append(atIndex);
			if ((i + 1) % ENTRIES_PER_PAGE == 0) {
				condensed.add(current);
				current = null;
			}
		}

		if (current != null)
			condensed.add(current);
		return condensed;
	}

	@Override
	public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
		MutableInt highest = new MutableInt(0);
		context.flapDisplayContext = highest;
		return provideEntries(context, stats.maxRows()).map(e -> {
			highest.setValue(Math.max(highest.getValue(), e.getFirst()));
			return createComponentsFromEntry(context, e);
		})
			.toList();
	}

	protected List<MutableComponent> createComponentsFromEntry(DisplayLinkContext context,
		IntAttached<MutableComponent> entry) {
		int number = entry.getFirst();
		MutableComponent name = entry.getSecond()
			.append(WHITESPACE);

		if (shortenNumbers(context)) {
			Couple<MutableComponent> shortened = shorten(number);
			return valueFirst() ? Arrays.asList(shortened.getFirst(), shortened.getSecond(), name)
				: Arrays.asList(name, shortened.getFirst(), shortened.getSecond());
		}

		MutableComponent formattedNumber = Components.literal(String.valueOf(number)).append(WHITESPACE);
		return valueFirst() ? Arrays.asList(formattedNumber, name) : Arrays.asList(name, formattedNumber);
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay,
		FlapDisplayLayout layout) {

		boolean valueFirst = valueFirst();
		boolean shortenNumbers = shortenNumbers(context);
		int valueFormat = shortenNumbers ? 0
			: Math.max(4, 1 + (int) Math.log10(((MutableInt) context.flapDisplayContext).intValue()));

		String layoutKey = "ValueList_" + valueFirst + "_" + valueFormat;
		if (layout.isLayout(layoutKey))
			return;

		int maxCharCount = flapDisplay.getMaxCharCount(1);
		int numberLength = Math.min(maxCharCount, Math.max(3, valueFormat));
		int nameLength = Math.max(maxCharCount - numberLength - (shortenNumbers ? 1 : 0), 0);

		FlapDisplaySection name = new FlapDisplaySection(MONOSPACE * nameLength, "alphabet", false, !valueFirst);
		FlapDisplaySection value =
			new FlapDisplaySection(MONOSPACE * numberLength, "number", false, !shortenNumbers && valueFirst)
				.rightAligned();

		if (shortenNumbers) {
			FlapDisplaySection suffix = new FlapDisplaySection(MONOSPACE, "shortened_numbers", false, valueFirst);
			layout.configure(layoutKey,
				valueFirst ? Arrays.asList(value, suffix, name) : Arrays.asList(name, value, suffix));
			return;
		}

		layout.configure(layoutKey, valueFirst ? Arrays.asList(value, name) : Arrays.asList(name, value));
	}

	private Couple<MutableComponent> shorten(int number) {
		if (number >= 1000000)
			return Couple.create(Components.literal(String.valueOf(number / 1000000)),
				Lang.translateDirect("display_source.value_list.million")
					.append(WHITESPACE));
		if (number >= 1000)
			return Couple.create(Components.literal(String.valueOf(number / 1000)),
				Lang.translateDirect("display_source.value_list.thousand")
					.append(WHITESPACE));
		return Couple.create(Components.literal(String.valueOf(number)), WHITESPACE);
	}

	protected boolean shortenNumbers(DisplayLinkContext context) {
		return context.sourceConfig()
			.getInt("Format") == 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		if (isFirstLine)
			addFullNumberConfig(builder);
	}

	@OnlyIn(Dist.CLIENT)
	protected void addFullNumberConfig(ModularGuiLineBuilder builder) {
		builder.addSelectionScrollInput(0, 75,
			(si, l) -> si.forOptions(Lang.translatedOptions("display_source.value_list", "shortened", "full_number"))
				.titled(Lang.translateDirect("display_source.value_list.display")),
			"Format");
	}

}
