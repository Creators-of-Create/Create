package com.simibubi.create.foundation.item;

import static com.simibubi.create.foundation.item.TooltipHelper.cutStringTextComponent;
import static com.simibubi.create.foundation.item.TooltipHelper.cutTextComponent;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.BLUE;
import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.DARK_GREEN;
import static net.minecraft.ChatFormatting.DARK_PURPLE;
import static net.minecraft.ChatFormatting.DARK_RED;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.LIGHT_PURPLE;
import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.STRIKETHROUGH;
import static net.minecraft.ChatFormatting.WHITE;
import static net.minecraft.ChatFormatting.YELLOW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.Block;

public class ItemDescription {

	public static final ItemDescription MISSING = new ItemDescription(null);
	public static Component trim = new TextComponent("                          ").withStyle(WHITE, STRIKETHROUGH);

	public enum Palette {

		Blue(BLUE, AQUA),
		Green(DARK_GREEN, GREEN),
		Yellow(GOLD, YELLOW),
		Red(DARK_RED, RED),
		Purple(DARK_PURPLE, LIGHT_PURPLE),
		Gray(DARK_GRAY, GRAY),

		;

		private Palette(ChatFormatting primary, ChatFormatting highlight) {
			color = primary;
			hColor = highlight;
		}

		public ChatFormatting color;
		public ChatFormatting hColor;
	}

	private List<Component> lines;
	private List<Component> linesOnShift;
	private List<Component> linesOnCtrl;
	private Palette palette;

	public ItemDescription(Palette palette) {
		this.palette = palette;
		lines = new ArrayList<>();
		linesOnShift = new ArrayList<>();
		linesOnCtrl = new ArrayList<>();
	}

	public ItemDescription withSummary(Component summary) {
		addStrings(linesOnShift, cutTextComponent(summary, palette.color, palette.hColor));
		return this;
	}

	public static List<Component> getKineticStats(Block block) {
		List<Component> list = new ArrayList<>();

		CKinetics config = AllConfigs.SERVER.kinetics;
		Component rpmUnit = Lang.translate("generic.unit.rpm");

		boolean hasGoggles = GogglesItem.isWearingGoggles(Minecraft.getInstance().player);

		SpeedLevel minimumRequiredSpeedLevel;
		boolean showStressImpact;
		if (!(block instanceof IRotate)) {
			minimumRequiredSpeedLevel = SpeedLevel.NONE;
			showStressImpact = true;
		} else {
			minimumRequiredSpeedLevel = ((IRotate) block).getMinimumRequiredSpeedLevel();
			showStressImpact = !((IRotate) block).hideStressImpact();
		}

//		boolean hasSpeedRequirement =
//			minimumRequiredSpeedLevel != SpeedLevel.NONE && minimumRequiredSpeedLevel != SpeedLevel.SLOW;
		boolean hasStressImpact =
			StressImpact.isEnabled() && showStressImpact && BlockStressValues.getImpact(block) > 0;
		boolean hasStressCapacity = StressImpact.isEnabled() && BlockStressValues.hasCapacity(block);

//		if (hasSpeedRequirement) {
//			int index = minimumRequiredSpeedLevel.ordinal();
//			MutableComponent level =
//				new TextComponent(makeProgressBar(3, index)).withStyle(minimumRequiredSpeedLevel.getTextColor());
//
//			if (hasGoggles)
//				level.append(String.valueOf(minimumRequiredSpeedLevel.getSpeedValue()))
//					.append(rpmUnit)
//					.append("+");
//			else
//				level.append(Lang.translate("tooltip.speedRequirement." + Lang.asId(minimumRequiredSpeedLevel.name())));
//
//			list.add(Lang.translate("tooltip.speedRequirement")
//				.withStyle(GRAY));
//			list.add(level);
//		}

		if (hasStressImpact) {
			double impact = BlockStressValues.getImpact(block);
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			int index = impactId.ordinal();
			MutableComponent level =
				new TextComponent(makeProgressBar(3, index + 1)).withStyle(impactId.getAbsoluteColor());

			if (hasGoggles)
				level.append(impact + "x ")
					.append(rpmUnit);
			else
				level.append(Lang.translate("tooltip.stressImpact." + Lang.asId(impactId.name())));

			list.add(Lang.translate("tooltip.stressImpact")
				.withStyle(GRAY));
			list.add(level);
		}

		if (hasStressCapacity) {
			double capacity = BlockStressValues.getCapacity(block);
			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.HIGH
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			StressImpact opposite = StressImpact.values()[2 - impactId.ordinal()];

			int index = StressImpact.values().length - 2 - impactId.ordinal();
			MutableComponent level =
				new TextComponent(makeProgressBar(3, impactId.ordinal() + 1)).withStyle(opposite.getAbsoluteColor());

			if (hasGoggles)
				level.append(capacity + "x ")
					.append(rpmUnit);
			else
				level.append(Lang.translate("tooltip.capacityProvided." + Lang.asId(impactId.name())));

//			if (!isEngine && ((IRotate) block).showCapacityWithAnnotation())
//				level +=
//					" " + DARK_GRAY + TextFormatting.ITALIC + Lang.translate("tooltip.capacityProvided.asGenerator");

			list.add(Lang.translate("tooltip.capacityProvided")
				.withStyle(GRAY));
			list.add(level);

			MutableComponent genSpeed = generatorSpeed(block, rpmUnit);
			if (!genSpeed.getString()
				.isEmpty())
				list.add(new TextComponent(" ").append(genSpeed)
					.withStyle(DARK_GRAY));
		}

		// if (hasSpeedRequirement || hasStressImpact || hasStressCapacity)
		// add(linesOnShift, "");
		return list;
	}

	public static String makeProgressBar(int length, int filledLength) {
		String bar = " ";
		int emptySpaces = length - filledLength;
		for (int i = 0; i < filledLength; i++)
			bar += "\u2588";
		for (int i = 0; i < emptySpaces; i++)
			bar += "\u2592";
		return bar + " ";
	}

	public ItemDescription withBehaviour(String condition, String behaviour) {
		add(linesOnShift, new TextComponent(condition).withStyle(GRAY));
		addStrings(linesOnShift, cutStringTextComponent(behaviour, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription withControl(String condition, String action) {
		add(linesOnCtrl, new TextComponent(condition).withStyle(GRAY));
		addStrings(linesOnCtrl, cutStringTextComponent(action, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription createTabs() {
		boolean hasDescription = !linesOnShift.isEmpty();
		boolean hasControls = !linesOnCtrl.isEmpty();

		if (hasDescription || hasControls) {
			String[] holdDesc = Lang.translate("tooltip.holdForDescription", "$")
				.getString()
				.split("\\$");
			String[] holdCtrl = Lang.translate("tooltip.holdForControls", "$")
				.getString()
				.split("\\$");
			MutableComponent keyShift = Lang.translate("tooltip.keyShift");
			MutableComponent keyCtrl = Lang.translate("tooltip.keyCtrl");
			for (List<Component> list : Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
				boolean shift = list == linesOnShift;
				boolean ctrl = list == linesOnCtrl;

				if (holdDesc.length != 2 || holdCtrl.length != 2) {
					list.add(0, new TextComponent("Invalid lang formatting!"));
					continue;
				}

				if (hasControls) {
					MutableComponent tabBuilder = new TextComponent("");
					tabBuilder.append(new TextComponent(holdCtrl[0]).withStyle(DARK_GRAY));
					tabBuilder.append(keyCtrl.plainCopy()
						.withStyle(ctrl ? WHITE : GRAY));
					tabBuilder.append(new TextComponent(holdCtrl[1]).withStyle(DARK_GRAY));
					list.add(0, tabBuilder);
				}

				if (hasDescription) {
					MutableComponent tabBuilder = new TextComponent("");
					tabBuilder.append(new TextComponent(holdDesc[0]).withStyle(DARK_GRAY));
					tabBuilder.append(keyShift.plainCopy()
						.withStyle(shift ? WHITE : GRAY));
					tabBuilder.append(new TextComponent(holdDesc[1]).withStyle(DARK_GRAY));
					list.add(0, tabBuilder);
				}

				if (shift || ctrl)
					list.add(hasDescription && hasControls ? 2 : 1, new TextComponent(""));
			}
		}

		if (!hasDescription)
			linesOnShift = lines;
		if (!hasControls)
			linesOnCtrl = lines;

		return this;
	}

	public static String hightlight(String s, Palette palette) {
		return palette.hColor + s + palette.color;
	}

	public static void addStrings(List<Component> infoList, List<Component> textLines) {
		textLines.forEach(s -> add(infoList, s));
	}

	public static void add(List<Component> infoList, List<Component> textLines) {
		infoList.addAll(textLines);
	}

	public static void add(List<Component> infoList, Component line) {
		infoList.add(line);
	}

	public Palette getPalette() {
		return palette;
	}

	public List<Component> addInformation(List<Component> tooltip) {
		if (Screen.hasShiftDown()) {
			tooltip.addAll(linesOnShift);
			return tooltip;
		}

		if (Screen.hasControlDown()) {
			tooltip.addAll(linesOnCtrl);
			return tooltip;
		}

		tooltip.addAll(lines);
		return tooltip;
	}

	public List<Component> getLines() {
		return lines;
	}

	public List<Component> getLinesOnCtrl() {
		return linesOnCtrl;
	}

	public List<Component> getLinesOnShift() {
		return linesOnShift;
	}

	private static MutableComponent generatorSpeed(Block block, Component unitRPM) {
		String value = "";

		if (block instanceof WaterWheelBlock) {
			int baseSpeed = AllConfigs.SERVER.kinetics.waterWheelBaseSpeed.get();
			int speedmod = AllConfigs.SERVER.kinetics.waterWheelFlowSpeed.get();
			value = (speedmod + baseSpeed) + "-" + (baseSpeed + (speedmod * 3));
		}

		return !value.equals("") ? Lang.translate("tooltip.generationSpeed", value, unitRPM)
			: TextComponent.EMPTY.plainCopy();
	}

}
