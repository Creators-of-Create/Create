package com.simibubi.create.content.trains.schedule.destination;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.PatternSyntaxException;

import com.google.re2j.Pattern;

import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.utility.LogisticParser;

import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.data.Glob;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.packagePort.postbox.PostboxBlockEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime.State;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.GlobalStation.GlobalPackagePort;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

public class FetchPackagesInstruction extends TextScheduleInstruction {

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(getSecondLineIcon(), CreateLang.translateDirect("schedule.instruction.package_retrieval"));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(CreateLang.translate("schedule.instruction.package_retrieval.summary")
			.style(ChatFormatting.GOLD)
			.component(), CreateLang.translateDirect("generic.in_quotes", Component.literal(getLabelText())),
			CreateLang.translateDirect("schedule.instruction.package_retrieval.summary_1")
				.withStyle(ChatFormatting.GRAY),
			CreateLang.translateDirect("schedule.instruction.package_retrieval.summary_2")
				.withStyle(ChatFormatting.GRAY));
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return PackageStyles.getDefaultBox();
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
		return data.getString("UseRegex").contains("Regex");
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
		return ImmutableList.of(CreateLang.translateDirect("schedule.instruction.address_filter_edit_box"),
			CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_1")
				.withStyle(ChatFormatting.GRAY),
			CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_2")
				.withStyle(ChatFormatting.DARK_GRAY),
			CreateLang.translateDirect("schedule.instruction.address_filter_edit_box_3")
				.withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected void modifyEditBox(EditBox box) {
		box.setFilter(s -> StringUtils.countMatches(s, '*') <= 3);
	}

	@Override
	public ResourceLocation getId() {
		return Create.asResource("package_retrieval");
	}

	@Override
	public boolean supportsConditions() {
		return false;
	}

	@Override
	public DiscoveredPath start(ScheduleRuntime runtime, Level level) {
		MinecraftServer server = level.getServer();
		if (server == null)
			return null;

		boolean anyMatch = false;
		ArrayList<GlobalStation> validStations = new ArrayList<>();
		Train train = runtime.train;

		if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
			train.status.missingConductor();
			runtime.startCooldown();
			return null;
		}

		for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
			ServerLevel dimLevel = server.getLevel(globalStation.blockEntityDimension);
			if (dimLevel == null)
				continue;
			Pattern regex = getFilterRegex();

			for (Entry<BlockPos, GlobalPackagePort> entry : globalStation.connectedPorts.entrySet()) {
				GlobalPackagePort port = entry.getValue();
				BlockPos pos = entry.getKey();

				IItemHandlerModifiable postboxInventory = port.offlineBuffer;
				if (dimLevel.isLoaded(pos) && dimLevel.getBlockEntity(pos) instanceof PostboxBlockEntity ppbe) {
					postboxInventory = ppbe.inventory;
				}

				for (int slot = 0; slot < postboxInventory.getSlots(); slot++) {
					ItemStack stack = postboxInventory.getStackInSlot(slot);
					if (!PackageItem.isPackage(stack))
						continue;
					if (PackageItem.matchAddress(stack, port.address, port.usesRegex))
						continue;
					try {
						if (!regex.matches(PackageItem.getAddress(stack)))
							continue;
						anyMatch = true;
						validStations.add(globalStation);
					} catch (PatternSyntaxException ignored) {
					}
				}
			}
		}

		if (validStations.isEmpty()) {
			runtime.startCooldown();
			runtime.state = State.PRE_TRANSIT;
			runtime.currentEntry++;
			return null;
		}

		DiscoveredPath best = train.navigation.findPathTo(validStations, Double.MAX_VALUE);
		if (best == null) {
			if (anyMatch)
				train.status.failedNavigation();
			runtime.startCooldown();
			return null;
		}

		return best;
	}

}
