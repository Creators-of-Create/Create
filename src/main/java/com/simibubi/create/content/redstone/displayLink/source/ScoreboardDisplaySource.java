package com.simibubi.create.content.redstone.displayLink.source;

import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.IntAttached;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;

public class ScoreboardDisplaySource extends ValueListDisplaySource {

	@Override
	protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
		Level level = context.blockEntity()
			.getLevel();
		if (!(level instanceof ServerLevel sLevel))
			return Stream.empty();

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
			.map(score -> IntAttached.with(score.getScore(), Components.literal(score.getOwner())
				.copy()))
			.sorted(IntAttached.comparator())
			.limit(maxRows);
	}

	private ImmutableList<IntAttached<MutableComponent>> notFound(String objective) {
		return ImmutableList
			.of(IntAttached.with(404, Lang.translateDirect("display_source.scoreboard.objective_not_found", objective)));
	}

	@Override
	protected String getTranslationKey() {
		return "scoreboard";
	}

	@Override
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
		if (isFirstLine)
			builder.addTextInput(0, 137, (e, t) -> {
				e.setValue("");
				t.withTooltip(ImmutableList.of(Lang.translateDirect("display_source.scoreboard.objective")
					.withStyle(s -> s.withColor(0x5391E1)),
					Lang.translateDirect("gui.schedule.lmb_edit")
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
