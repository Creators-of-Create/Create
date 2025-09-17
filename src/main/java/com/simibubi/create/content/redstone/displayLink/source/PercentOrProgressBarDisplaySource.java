package com.simibubi.create.content.redstone.displayLink.source;

import static com.simibubi.create.content.trains.display.FlapDisplaySection.WIDE_MONOSPACE;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplaySection;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public abstract class PercentOrProgressBarDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		Float rawProgress = this.getProgress(context);
		if (rawProgress == null)
			return EMPTY_LINE;

		if (!progressBarActive(context))
			return formatNumeric(context, rawProgress);

		String label = context.sourceConfig()
			.getString("Label");

		int labelSize = label.isEmpty() ? 0 : label.length() + 1;
		int length = Math.min(stats.maxColumns() - labelSize, 128);

		if (context.getTargetBlockEntity() instanceof SignBlockEntity)
			length = (int) (length * 6f / 9f);
		if (context.getTargetBlockEntity() instanceof FlapDisplayBlockEntity)
			length = sizeForWideChars(length);

		// clamp just in case - #7371
		float currentLevel = Mth.clamp(rawProgress, 0, 1);
		int filledLength = (int) (currentLevel * length);

		if (length < 1)
			return EMPTY_LINE;

		int emptySpaces = length - filledLength;
		String s = "\u2588".repeat(Math.max(0, filledLength)) +
			"\u2592".repeat(Math.max(0, emptySpaces));
		return Component.literal(s);
	}

	protected MutableComponent formatNumeric(DisplayLinkContext context, Float currentLevel) {
		return Component.literal(Mth.clamp((int) (currentLevel * 100), 0, 100) + "%");
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
