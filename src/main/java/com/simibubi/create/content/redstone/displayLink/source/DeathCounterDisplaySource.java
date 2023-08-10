package com.simibubi.create.content.redstone.displayLink.source;

import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;

public class DeathCounterDisplaySource extends StatTrackingDisplaySource {

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
		return CreateLang.translateDirect("display_source.scoreboard.objective.deaths");
	}

}
