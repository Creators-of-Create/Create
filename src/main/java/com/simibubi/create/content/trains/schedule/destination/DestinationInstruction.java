package com.simibubi.create.content.trains.schedule.destination;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.re2j.Pattern;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.LogisticParser;

import com.simibubi.create.infrastructure.config.AllConfigs;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

public class DestinationInstruction extends TextScheduleInstruction {

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(AllBlocks.TRACK_STATION.asStack(), Component.literal(getLabelText()));
	}

	@Override
	public boolean supportsConditions() {
		return true;
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("destination");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return AllBlocks.TRACK_STATION.asStack();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		List<@NotNull String> scrollInputOptions = AllConfigs.server().logistics.enableAdvancedRegex.get()
			? List.of("use_glob", "use_regex")
			: List.of("use_glob");

		builder.addTextInput(0, 103-18-9, (e, t) -> modifyEditBox(e), "Text")
			.addSelectionScrollInput(103-22,
				40,
				(si, l) -> si.forOptions(CreateLang
						.translatedOptions("gui.schedule.fetch_packages",
							scrollInputOptions))
					.titled(CreateLang
						.translate("gui.schedule.fetch_packages.address_matching")
						.component()),
				"UseRegex");
	}

	public boolean useRegex() {
		Create.LOGGER.info("Regex key was " + data.getInt("UseRegex"));
		return data.getInt("UseRegex") == 1;
	}

	public String getFilter() {
		return getLabelText();
	}

	public Pattern getFilterRegex() {
		if (getFilter().isBlank())
			return Pattern.compile(".*");
		return LogisticParser.dynamicToRegex(getFilter(), "", useRegex());
	}

	@Override
	public List<Component> getSecondLineTooltip(int slot) {
		return ImmutableList.of(CreateLang.translateDirect("schedule.instruction.filter_edit_box"),
			CreateLang.translateDirect("schedule.instruction.filter_edit_box_1")
				.withStyle(ChatFormatting.GRAY),
			CreateLang.translateDirect("schedule.instruction.filter_edit_box_2")
				.withStyle(ChatFormatting.DARK_GRAY),
			CreateLang.translateDirect("schedule.instruction.filter_edit_box_3")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {
		box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
	}

	@Override
	@Nullable
	public DiscoveredPath start(ScheduleRuntime runtime, Level level) {
		Pattern regex = getFilterRegex();
		boolean anyMatch = false;
		ArrayList<GlobalStation> validStations = new ArrayList<>();
		Train train = runtime.train;

		if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
			train.status.missingConductor();
			runtime.startCooldown();
			return null;
		}


		for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
			if (!LogisticParser.anyMatches(regex, globalStation.name))
				continue;
			anyMatch = true;
			validStations.add(globalStation);
		}

		DiscoveredPath best = train.navigation.findPathTo(validStations, Double.MAX_VALUE);
		if (best == null) {
			if (anyMatch)
				train.status.failedNavigation();
			else
				train.status.failedNavigationNoTarget(getFilter());
			runtime.startCooldown();
			return null;
		}

		return best;
	}

}
