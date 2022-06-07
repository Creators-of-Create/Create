package com.simibubi.create.content.logistics.block.display.source;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.simibubi.create.content.contraptions.fluids.tank.BoilerData;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayTileEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class BoilerDisplaySource extends DisplaySource {

	public static final List<MutableComponent> notEnoughSpaceSingle = List.of(new TextComponent("Not enough space for Boiler Status!"));
	public static final List<MutableComponent> notEnoughSpaceDouble = List.of(new TextComponent("Not enough space"), new TextComponent("for Boiler Status!"));
	public static final List<List<MutableComponent>> notEnoughSpaceFlap = List.of(List.of(new TextComponent("Not enough space")), List.of(new TextComponent("for Boiler Status!")));

	@Override
	public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
		if (stats.maxRows() < 2) {
			return notEnoughSpaceSingle;
		} else if (stats.maxRows() < 4) {
			return notEnoughSpaceDouble;
		}

		boolean isBook = context.getTargetTE() instanceof LecternBlockEntity;

		if (isBook) {
			Stream<MutableComponent> componentList = getComponents(context, false)
					.map(components -> {
						Optional<MutableComponent> reduce = components.stream().reduce(MutableComponent::append);
						return reduce.orElse(EMPTY_LINE);
					});

			return List.of(componentList.reduce((comp1, comp2) -> comp1.append(new TextComponent("\n")).append(comp2)).orElse(EMPTY_LINE));
		}

		return getComponents(context, false)
				.map(components -> {
					Optional<MutableComponent> reduce = components.stream().reduce(MutableComponent::append);
					return reduce.orElse(EMPTY_LINE);
				}).toList();
	}

	@Override
	public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
		if (stats.maxRows() < 4) {
			context.flapDisplayContext = Boolean.FALSE;
			return notEnoughSpaceFlap;
		}

		List<List<MutableComponent>> components = getComponents(context, true).toList();

		if (stats.maxColumns() * FlapDisplaySection.MONOSPACE < 6 * FlapDisplaySection.MONOSPACE + components.get(1).get(1).getString().length() * FlapDisplaySection.WIDE_MONOSPACE) {
			context.flapDisplayContext = Boolean.FALSE;
			return notEnoughSpaceFlap;
		}

		return components;
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayTileEntity flapDisplay, FlapDisplayLayout layout, int lineIndex) {
		if (lineIndex == 0 || context.flapDisplayContext instanceof Boolean b && !b) {
			if (layout.isLayout("Default"))
				return;

			layout.loadDefault(flapDisplay.getMaxCharCount());
			return;
		}

		String layoutKey = "Boiler";
		if (layout.isLayout(layoutKey))
			return;

		int labelLength = (int) (5 * FlapDisplaySection.MONOSPACE);
		float maxSpace = flapDisplay.getMaxCharCount(1) * FlapDisplaySection.MONOSPACE;
		FlapDisplaySection label = new FlapDisplaySection(labelLength, "alphabet", false, true);
		FlapDisplaySection symbols = new FlapDisplaySection(maxSpace - labelLength, "pixel", false, false).wideFlaps();

		layout.configure(layoutKey, List.of(label, symbols));
	}

	private Stream<List<MutableComponent>> getComponents(DisplayLinkContext context, boolean forFlapDisplay) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof FluidTankTileEntity tankTile))
			return Stream.of(EMPTY);

		tankTile = tankTile.getControllerTE();
		if (tankTile == null)
			return Stream.of(EMPTY);

		BoilerData boiler = tankTile.boiler;

		int totalTankSize = tankTile.getTotalTankSize();

		boiler.calcMinMaxForSize(totalTankSize);

		String label = forFlapDisplay ? "Boiler Status: " : "Boiler:";
		String size = forFlapDisplay ? " Size" : "";
		String water = forFlapDisplay ? "Water" : "";
		String heat = forFlapDisplay ? " Heat" : "";

		//String size = forFlapDisplay ? " Size" : "\u21d5";
		//String water = forFlapDisplay ? "Water" : "\ud83c\udf0a";
		//String heat = forFlapDisplay ? " Heat" : "\ud83d\udd25";

		return Stream.of(
				List.of(new TextComponent(label).append(boiler.getHeatLevelTextComponent())),
				List.of(new TextComponent(size), boiler.getSizeComponent(!forFlapDisplay, forFlapDisplay, ChatFormatting.BLACK)),
				List.of(new TextComponent(water), boiler.getWaterComponent(!forFlapDisplay, forFlapDisplay, ChatFormatting.BLACK)),
				List.of(new TextComponent(heat), boiler.getHeatComponent(!forFlapDisplay, forFlapDisplay, ChatFormatting.BLACK))
		);
	}

	@Override
	protected String getTranslationKey() {
		return "boiler_status";
	}
}
