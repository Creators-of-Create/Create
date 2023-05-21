package com.simibubi.create.content.redstone.displayLink.source;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayLayout;
import com.simibubi.create.content.trains.display.FlapDisplaySection;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SingleLineDisplaySource extends DisplaySource {

	protected abstract MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats);

	protected abstract boolean allowsLabeling(DisplayLinkContext context);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		if (isFirstLine && allowsLabeling(context))
			addLabelingTextBox(builder);
	}

	@OnlyIn(Dist.CLIENT)
	protected void addLabelingTextBox(ModularGuiLineBuilder builder) {
		builder.addTextInput(0, 137, (e, t) -> {
			e.setValue("");
			t.withTooltip(ImmutableList.of(Lang.translateDirect("display_source.label")
				.withStyle(s -> s.withColor(0x5391E1)),
				Lang.translateDirect("gui.schedule.lmb_edit")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
		}, "Label");
	}

	@Override
	public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
		MutableComponent line = provideLine(context, stats);
		if (line == EMPTY_LINE)
			return EMPTY;

		if (allowsLabeling(context)) {
			String label = context.sourceConfig()
				.getString("Label");
			if (!label.isEmpty())
				line = Components.literal(label + " ").append(line);
		}

		return ImmutableList.of(line);
	}

	@Override
	public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {

		if (allowsLabeling(context)) {
			String label = context.sourceConfig()
				.getString("Label");
			if (!label.isEmpty())
				return ImmutableList.of(ImmutableList.of(Components.literal(label + " "), provideLine(context, stats)));
		}

		return super.provideFlapDisplayText(context, stats);
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay,
		FlapDisplayLayout layout) {
		String layoutKey = getFlapDisplayLayoutName(context);

		if (!allowsLabeling(context)) {
			if (!layout.isLayout(layoutKey))
				layout.configure(layoutKey,
					ImmutableList.of(createSectionForValue(context, flapDisplay.getMaxCharCount())));
			return;
		}

		String label = context.sourceConfig()
			.getString("Label");

		if (label.isEmpty()) {
			if (!layout.isLayout(layoutKey))
				layout.configure(layoutKey,
					ImmutableList.of(createSectionForValue(context, flapDisplay.getMaxCharCount())));
			return;
		}

		String layoutName = label.length() + "_Labeled_" + layoutKey;
		if (layout.isLayout(layoutName))
			return;

		int maxCharCount = flapDisplay.getMaxCharCount();
		FlapDisplaySection labelSection = new FlapDisplaySection(
			Math.min(maxCharCount, label.length() + 1) * FlapDisplaySection.MONOSPACE, "alphabet", false, false);

		if (label.length() + 1 < maxCharCount)
			layout.configure(layoutName,
				ImmutableList.of(labelSection, createSectionForValue(context, maxCharCount - label.length() - 1)));
		else
			layout.configure(layoutName, ImmutableList.of(labelSection));
	}

	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		return "Default";
	}

	protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "alphabet", false, false);
	}

}
