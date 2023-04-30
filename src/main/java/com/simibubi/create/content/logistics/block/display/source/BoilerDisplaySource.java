package com.simibubi.create.content.logistics.block.display.source;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.simibubi.create.content.contraptions.fluids.tank.BoilerData;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplayLayout;
import com.simibubi.create.content.logistics.trains.management.display.FlapDisplaySection;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

public class BoilerDisplaySource extends DisplaySource {

	public static final List<MutableComponent> notEnoughSpaceSingle =
		List.of(Lang.translateDirect("display_source.boiler.not_enough_space")
			.append(Lang.translateDirect("display_source.boiler.for_boiler_status")));

	public static final List<MutableComponent> notEnoughSpaceDouble =
		List.of(Lang.translateDirect("display_source.boiler.not_enough_space"),
			Lang.translateDirect("display_source.boiler.for_boiler_status"));

	public static final List<List<MutableComponent>> notEnoughSpaceFlap =
		List.of(List.of(Lang.translateDirect("display_source.boiler.not_enough_space")),
			List.of(Lang.translateDirect("display_source.boiler.for_boiler_status")));

	@Override
	public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
		if (stats.maxRows() < 2)
			return notEnoughSpaceSingle;
		else if (stats.maxRows() < 4)
			return notEnoughSpaceDouble;

		boolean isBook = context.getTargetBlockEntity() instanceof LecternBlockEntity;

		if (isBook) {
			Stream<MutableComponent> componentList = getComponents(context, false).map(components -> {
				Optional<MutableComponent> reduce = components.stream()
					.reduce(MutableComponent::append);
				return reduce.orElse(EMPTY_LINE);
			});

			return List.of(componentList.reduce((comp1, comp2) -> comp1.append(Components.literal("\n"))
				.append(comp2))
				.orElse(EMPTY_LINE));
		}

		return getComponents(context, false).map(components -> {
			Optional<MutableComponent> reduce = components.stream()
				.reduce(MutableComponent::append);
			return reduce.orElse(EMPTY_LINE);
		})
			.toList();
	}

	@Override
	public List<List<MutableComponent>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
		if (stats.maxRows() < 4) {
			context.flapDisplayContext = Boolean.FALSE;
			return notEnoughSpaceFlap;
		}

		List<List<MutableComponent>> components = getComponents(context, true).toList();

		if (stats.maxColumns() * FlapDisplaySection.MONOSPACE < 6 * FlapDisplaySection.MONOSPACE + components.get(1)
			.get(1)
			.getString()
			.length() * FlapDisplaySection.WIDE_MONOSPACE) {
			context.flapDisplayContext = Boolean.FALSE;
			return notEnoughSpaceFlap;
		}

		return components;
	}

	@Override
	public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay,
		FlapDisplayLayout layout, int lineIndex) {
		if (lineIndex == 0 || context.flapDisplayContext instanceof Boolean b && !b) {
			if (layout.isLayout("Default"))
				return;

			layout.loadDefault(flapDisplay.getMaxCharCount());
			return;
		}

		String layoutKey = "Boiler";
		if (layout.isLayout(layoutKey))
			return;

		int labelLength = (int) (labelWidth() * FlapDisplaySection.MONOSPACE);
		float maxSpace = flapDisplay.getMaxCharCount(1) * FlapDisplaySection.MONOSPACE;
		FlapDisplaySection label = new FlapDisplaySection(labelLength, "alphabet", false, true);
		FlapDisplaySection symbols = new FlapDisplaySection(maxSpace - labelLength, "pixel", false, false).wideFlaps();

		layout.configure(layoutKey, List.of(label, symbols));
	}

	private Stream<List<MutableComponent>> getComponents(DisplayLinkContext context, boolean forFlapDisplay) {
		BlockEntity sourceBE = context.getSourceBlockEntity();
		if (!(sourceBE instanceof FluidTankBlockEntity tankBlockEntity))
			return Stream.of(EMPTY);

		tankBlockEntity = tankBlockEntity.getControllerBE();
		if (tankBlockEntity == null)
			return Stream.of(EMPTY);

		BoilerData boiler = tankBlockEntity.boiler;

		int totalTankSize = tankBlockEntity.getTotalTankSize();

		boiler.calcMinMaxForSize(totalTankSize);

		String label = forFlapDisplay ? "boiler.status" : "boiler.status_short";
		MutableComponent size = labelOf(forFlapDisplay ? "size" : "");
		MutableComponent water = labelOf(forFlapDisplay ? "water" : "");
		MutableComponent heat = labelOf(forFlapDisplay ? "heat" : "");

		int lw = labelWidth();
		if (forFlapDisplay) {
			size = Components.literal(Strings.repeat(' ', lw - labelWidthOf("size"))).append(size);
			water = Components.literal(Strings.repeat(' ', lw - labelWidthOf("water"))).append(water);
			heat = Components.literal(Strings.repeat(' ', lw - labelWidthOf("heat"))).append(heat);
		}

		return Stream.of(List.of(Lang.translateDirect(label, boiler.getHeatLevelTextComponent())),
			List.of(size, boiler.getSizeComponent(!forFlapDisplay, forFlapDisplay, ChatFormatting.RESET)),
			List.of(water, boiler.getWaterComponent(!forFlapDisplay, forFlapDisplay, ChatFormatting.RESET)),
			List.of(heat, boiler.getHeatComponent(!forFlapDisplay, forFlapDisplay, ChatFormatting.RESET)));
	}

	private int labelWidth() {
		return Math.max(labelWidthOf("water"), Math.max(labelWidthOf("size"), labelWidthOf("heat")));
	}

	private int labelWidthOf(String label) {
		return labelOf(label).getString()
			.length();
	}

	private MutableComponent labelOf(String label) {
		if (label.isBlank())
			return Components.empty();
		return Lang.translateDirect("boiler." + label);
	}

	@Override
	protected String getTranslationKey() {
		return "boiler_status";
	}
}
