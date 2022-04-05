package com.simibubi.create.content.logistics.block.data.source;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.DataGathererScreen.LineBuilder;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;

public class ScoreboardDataSource extends ValueListDataSource {

	@Override
	protected Stream<IntAttached<MutableComponent>> provideEntries(DataGathererContext context, int maxRows) {
		Level level = context.te()
			.getLevel();
		if (!(level instanceof ServerLevel sLevel))
			return new ArrayList<IntAttached<MutableComponent>>().stream();

		String name = context.sourceConfig()
			.getString("Objective");

		return showScoreboard(sLevel, name, maxRows);
	}

	protected Stream<IntAttached<MutableComponent>> showScoreboard(ServerLevel sLevel, String objectiveName,
		int maxRows) {
		Objective objective = sLevel.getScoreboard()
			.getObjective(objectiveName);
		if (objective == null)
			return notFound(objectiveName).stream();

		return sLevel.getScoreboard()
			.getPlayerScores(objective)
			.stream()
			.limit(maxRows)
			.map(score -> IntAttached.with(score.getScore(), new TextComponent(score.getOwner()).copy()))
			.sorted(IntAttached.comparator());
	}

	private ImmutableList<IntAttached<MutableComponent>> notFound(String objective) {
		return ImmutableList
			.of(IntAttached.with(404, Lang.translate("data_source.scoreboard.objective_not_found", objective)));
	}
	
	@Override
	protected String getTranslationKey() {
		return "scoreboard";
	}

	@Override
	public void initConfigurationWidgets(DataGathererContext context, LineBuilder builder, boolean isFirstLine) {
		if (isFirstLine)
			builder.addTextInput(0, 137, (e, t) -> {
				e.setValue("");
				t.withTooltip(ImmutableList.of(Lang.translate("data_source.scoreboard.objective")
					.withStyle(s -> s.withColor(0x5391E1)),
					Lang.translate("gui.schedule.lmb_edit")
						.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
			}, "Objective");
		else
			addFullNumberConfig(builder);
	}

	@Override
	protected boolean valueFirst() {
		return false;
	}

}
