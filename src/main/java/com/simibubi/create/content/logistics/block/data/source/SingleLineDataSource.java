package com.simibubi.create.content.logistics.block.data.source;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererScreen.LineBuilder;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SingleLineDataSource extends DataGathererSource {

	protected abstract MutableComponent provideLine(DataGathererContext context, DataTargetStats stats);

	protected abstract boolean allowsLabeling(DataGathererContext context);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DataGathererContext context, LineBuilder builder, boolean isFirstLine) {
		if (isFirstLine && allowsLabeling(context))
			addLabelingTextBox(builder);
	}

	@OnlyIn(Dist.CLIENT)
	protected void addLabelingTextBox(LineBuilder builder) {
		builder.addTextInput(0, 137, (e, t) -> {
			e.setValue("");
			t.withTooltip(ImmutableList.of(Lang.translate("data_source.label")
				.withStyle(s -> s.withColor(0x5391E1)),
				Lang.translate("gui.schedule.lmb_edit")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
		}, "Label");
	}

	@Override
	public List<MutableComponent> provideText(DataGathererContext context, DataTargetStats stats) {
		MutableComponent line = provideLine(context, stats);
		if (line == EMPTY_LINE)
			return EMPTY;

		if (allowsLabeling(context)) {
			String label = context.sourceConfig()
				.getString("Label");
			if (!label.isEmpty())
				line = new TextComponent(label + " ").append(line);
		}

		return ImmutableList.of(line);
	}

	@Override
	public List<List<MutableComponent>> provideFlapDisplayText(DataGathererContext context, DataTargetStats stats) {

		if (allowsLabeling(context)) {
			String label = context.sourceConfig()
				.getString("Label");
			if (!label.isEmpty())
				return ImmutableList.of(ImmutableList.of(new TextComponent(label + " "), provideLine(context, stats)));
		}

		return super.provideFlapDisplayText(context, stats);
	}

	@Override
	public void loadFlapDisplayLayout(DataGathererContext context, FlapDisplayTileEntity flapDisplay,
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

	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		return "Default";
	}

	protected FlapDisplaySection createSectionForValue(DataGathererContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "alphabet", false, false);
	}

}
