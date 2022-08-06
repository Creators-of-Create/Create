package com.simibubi.create.content.logistics.block.display.source;

import static com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection.WIDE_MONOSPACE;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public abstract class PercentOrProgressBarDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		Float currentLevel = getProgress(context);
		if (currentLevel == null)
			return EMPTY_LINE;
		if (!progressBarActive(context))
			return formatNumeric(context, currentLevel);

		String label = context.sourceConfig()
			.getString("Label");

		int labelSize = label.isEmpty() ? 0 : label.length() + 1;
		int length = Math.min(stats.maxColumns() - labelSize, 128);

		if (context.getTargetTE() instanceof SignBlockEntity)
			length = (int) (length * 6f / 9f);
		if (context.getTargetTE() instanceof FlapDisplayTileEntity)
			length = sizeForWideChars(length);

		int filledLength = (int) (currentLevel * length);

		if (length < 1)
			return EMPTY_LINE;

		StringBuilder s = new StringBuilder();
		int emptySpaces = length - filledLength;
		for (int i = 0; i < filledLength; i++)
			s.append("\u2588");
		for (int i = 0; i < emptySpaces; i++)
			s.append("\u2592");

		return Components.literal(s.toString());
	}

	protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
		return Components.literal(Mth.clamp((int) (currentLevel * 100), 0, 100) + "%");
	}

	@Nullable
	protected abstract Float getProgress(DisplayLinkContext context);

	protected abstract boolean progressBarActive(DisplayLinkContext context);

	@Override
	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		return !progressBarActive(context) ? super.getFlapDisplayLayoutName(context) : "Progress";
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
		return !progressBarActive(context) ? super.createSectionForValue(context, size)
			: new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "pixel", false, false).wideFlaps();
	}

	private int sizeForWideChars(int size) {
		return (int) (size * FlapDisplaySection.MONOSPACE / WIDE_MONOSPACE);
	}

}
