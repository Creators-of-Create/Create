package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.content.kinetics.clock.CuckooClockBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.content.trains.display.FlapDisplaySection;

import net.createmod.catnip.utility.lang.Components;
import net.minecraft.network.chat.MutableComponent;

public class StopWatchDisplaySource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof CuckooClockBlockEntity ccbe))
			return TimeOfDayDisplaySource.EMPTY_TIME;
		if (ccbe.getSpeed() == 0)
			return TimeOfDayDisplaySource.EMPTY_TIME;

		if (!context.sourceConfig()
			.contains("StartTime"))
			onSignalReset(context);

		long started = context.sourceConfig()
			.getLong("StartTime");
		long current = context.blockEntity()
			.getLevel()
			.getGameTime();

		int diff = (int) (current - started);
		int hours = (diff / 60 / 60 / 20);
		int minutes = (diff / 60 / 20) % 60;
		int seconds = (diff / 20) % 60;

		MutableComponent component = Components.literal((hours == 0 ? "" : (hours < 10 ? " " : "") + hours + ":")
			+ (minutes < 10 ? hours == 0 ? " " : "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);

		return component;
	}

	@Override
	public void onSignalReset(DisplayLinkContext context) {
		context.sourceConfig()
			.putLong("StartTime", context.blockEntity()
				.getLevel()
				.getGameTime());
	}

	@Override
	public int getPassiveRefreshTicks() {
		return 20;
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
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
		return "stop_watch";
	}

}
