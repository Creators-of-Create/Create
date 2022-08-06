package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TimeOfDayDisplaySource extends SingleLineDisplaySource {

	public static final MutableComponent EMPTY_TIME = Components.literal("--:--");
	
	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.level()instanceof ServerLevel sLevel))
			return EMPTY_TIME;
		if (!(context.getSourceTE() instanceof CuckooClockTileEntity ccte))
			return EMPTY_TIME;
		if (ccte.getSpeed() == 0)
			return EMPTY_TIME;

		boolean c12 = context.sourceConfig()
			.getInt("Cycle") == 0;
		boolean isNatural = sLevel.dimensionType()
			.natural();

		int dayTime = (int) (sLevel.getDayTime() % 24000);
		int hours = (dayTime / 1000 + 6) % 24;
		int minutes = (dayTime % 1000) * 60 / 1000;
		MutableComponent suffix = Lang.translateDirect("generic.daytime." + (hours > 11 ? "pm" : "am"));

		minutes = minutes / 5 * 5;
		if (c12) {
			hours %= 12;
			if (hours == 0)
				hours = 12;
		}

		if (!isNatural) {
			hours = Create.RANDOM.nextInt(70) + 24;
			minutes = Create.RANDOM.nextInt(40) + 60;
		}

		MutableComponent component = Components.literal(
			(hours < 10 ? " " : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + (c12 ? " " : ""));

		return c12 ? component.append(suffix) : component;
	}

	@Override
	protected String getFlapDisplayLayoutName(DisplayLinkContext context) {
		return "Instant";
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DisplayLinkContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "instant", false, false);
	}

	@Override
	protected String getTranslationKey() {
		return "time_of_day";
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		super.initConfigurationWidgets(context, builder, isFirstLine);
		if (isFirstLine)
			return;

		builder.addSelectionScrollInput(0, 60, (si, l) -> {
			si.forOptions(Lang.translatedOptions("display_source.time", "12_hour", "24_hour"))
				.titled(Lang.translateDirect("display_source.time.format"));
		}, "Cycle");
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
