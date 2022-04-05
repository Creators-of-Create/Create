package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

public class DeathCounterDataSource extends StatTrackingDataSource {

	@Override
	protected int updatedScoreOf(ServerPlayer player) {
		return player.getStats()
			.getValue(Stats.CUSTOM.get(Stats.DEATHS));
	}
	
	@Override
	protected String getTranslationKey() {
		return "player_deaths";
	}

	@Override
	protected String getObjectiveName() {
		return "deaths";
	}

	@Override
	protected Component getObjectiveDisplayName() {
		return Lang.translate("data_source.scoreboard.objective.deaths");
	}

}
