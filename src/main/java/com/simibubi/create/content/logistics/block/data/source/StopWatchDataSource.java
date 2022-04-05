package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.content.contraptions.components.clock.CuckooClockTileEntity;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public class StopWatchDataSource extends SingleLineDataSource {

	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		if (!(context.getSourceTE()instanceof CuckooClockTileEntity ccte))
			return TimeOfDayDataSource.EMPTY_TIME;
		if (ccte.getSpeed() == 0)
			return TimeOfDayDataSource.EMPTY_TIME;

		if (!context.sourceConfig()
			.contains("StartTime"))
			onSignalReset(context);

		long started = context.sourceConfig()
			.getLong("StartTime");
		long current = context.te()
			.getLevel()
			.getGameTime();

		int diff = (int) (current - started);
		int hours = (diff / 60 / 60 / 20);
		int minutes = (diff / 60 / 20) % 60;
		int seconds = (diff / 20) % 60;

		MutableComponent component = new TextComponent((hours == 0 ? "" : (hours < 10 ? " " : "") + hours + ":")
			+ (minutes < 10 ? hours == 0 ? " " : "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);

		return component;
	}

	@Override
	public void onSignalReset(DataGathererContext context) {
		context.sourceConfig()
			.putLong("StartTime", context.te()
				.getLevel()
				.getGameTime());
	}

	@Override
	public int getPassiveRefreshTicks() {
		return 20;
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return true;
	}

	@Override
	protected String getFlapDisplayLayoutName(DataGathererContext context) {
		return "Instant";
	}

	@Override
	protected FlapDisplaySection createSectionForValue(DataGathererContext context, int size) {
		return new FlapDisplaySection(size * FlapDisplaySection.MONOSPACE, "instant", false, false);
	}

	@Override
	protected String getTranslationKey() {
		return "stop_watch";
	}

}
