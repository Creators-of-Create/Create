package com.simibubi.create.foundation.item;

import static com.simibubi.create.foundation.item.TooltipHelper.cutStringTextComponent;
import static com.simibubi.create.foundation.item.TooltipHelper.cutTextComponent;
import static net.minecraft.util.text.TextFormatting.AQUA;
import static net.minecraft.util.text.TextFormatting.BLUE;
import static net.minecraft.util.text.TextFormatting.DARK_GRAY;
import static net.minecraft.util.text.TextFormatting.DARK_GREEN;
import static net.minecraft.util.text.TextFormatting.DARK_PURPLE;
import static net.minecraft.util.text.TextFormatting.DARK_RED;
import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GRAY;
import static net.minecraft.util.text.TextFormatting.GREEN;
import static net.minecraft.util.text.TextFormatting.LIGHT_PURPLE;
import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.STRIKETHROUGH;
import static net.minecraft.util.text.TextFormatting.WHITE;
import static net.minecraft.util.text.TextFormatting.YELLOW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemDescription {

	public static final ItemDescription MISSING = new ItemDescription(null);
	public static ITextComponent trim =
		new StringTextComponent("                          ").withStyle(WHITE, STRIKETHROUGH);

	public enum Palette {

		Blue(BLUE, AQUA),
		Green(DARK_GREEN, GREEN),
		Yellow(GOLD, YELLOW),
		Red(DARK_RED, RED),
		Purple(DARK_PURPLE, LIGHT_PURPLE),
		Gray(DARK_GRAY, GRAY),

		;

		private Palette(TextFormatting primary, TextFormatting highlight) {
			color = primary;
			hColor = highlight;
		}

		public TextFormatting color;
		public TextFormatting hColor;
	}

	private List<ITextComponent> lines;
	private List<ITextComponent> linesOnShift;
	private List<ITextComponent> linesOnCtrl;
	private Palette palette;

	public ItemDescription(Palette palette) {
		this.palette = palette;
		lines = new ArrayList<>();
		linesOnShift = new ArrayList<>();
		linesOnCtrl = new ArrayList<>();
	}

	public ItemDescription withSummary(ITextComponent summary) {
		addStrings(linesOnShift, cutTextComponent(summary, palette.color, palette.hColor));
		return this;
	}

	public static List<ITextComponent> getKineticStats(Block block) {
		List<ITextComponent> list = new ArrayList<>();

		CKinetics config = AllConfigs.SERVER.kinetics;
		ITextComponent rpmUnit = Lang.translate("generic.unit.rpm");

		boolean hasGoggles =
			AllItems.GOGGLES.isIn(Minecraft.getInstance().player.getItemBySlot(EquipmentSlotType.HEAD));

		SpeedLevel minimumRequiredSpeedLevel;
		boolean showStressImpact;
		if (!(block instanceof IRotate)) {
			minimumRequiredSpeedLevel = SpeedLevel.NONE;
			showStressImpact = true;
		} else {
			minimumRequiredSpeedLevel = ((IRotate) block).getMinimumRequiredSpeedLevel();
			showStressImpact = !((IRotate) block).hideStressImpact();
		}

		boolean hasSpeedRequirement = minimumRequiredSpeedLevel != SpeedLevel.NONE;
		boolean hasStressImpact = StressImpact.isEnabled() && showStressImpact && BlockStressValues.getImpact(block) > 0;
		boolean hasStressCapacity = StressImpact.isEnabled() && BlockStressValues.hasCapacity(block);

		if (hasSpeedRequirement) {
			List<ITextComponent> speedLevels =
				Lang.translatedOptions("tooltip.speedRequirement", "none", "medium", "high");
			int index = minimumRequiredSpeedLevel.ordinal();
			IFormattableTextComponent level =
				new StringTextComponent(makeProgressBar(3, index)).withStyle(minimumRequiredSpeedLevel.getTextColor());

			if (hasGoggles)
				level.append(String.valueOf(minimumRequiredSpeedLevel.getSpeedValue()))
					.append(rpmUnit)
					.append("+");
			else
				level.append(speedLevels.get(index));

			list.add(Lang.translate("tooltip.speedRequirement")
				.withStyle(GRAY));
			list.add(level);
		}

		if (hasStressImpact) {
			List<ITextComponent> stressLevels = Lang.translatedOptions("tooltip.stressImpact", "low", "medium", "high");
			double impact = BlockStressValues.getImpact(block);
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			int index = impactId.ordinal();
			IFormattableTextComponent level =
				new StringTextComponent(makeProgressBar(3, index)).withStyle(impactId.getAbsoluteColor());

			if (hasGoggles)
				level.append(impact + "x ")
					.append(rpmUnit);
			else
				level.append(stressLevels.get(index));

			list.add(Lang.translate("tooltip.stressImpact")
				.withStyle(GRAY));
			list.add(level);
		}

		if (hasStressCapacity) {
			List<ITextComponent> stressCapacityLevels =
				Lang.translatedOptions("tooltip.capacityProvided", "low", "medium", "high");
			double capacity = BlockStressValues.getCapacity(block);
			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.LOW
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.HIGH);
			int index = StressImpact.values().length - 2 - impactId.ordinal();
			IFormattableTextComponent level =
				new StringTextComponent(makeProgressBar(3, index)).withStyle(impactId.getAbsoluteColor());

			if (hasGoggles)
				level.append(capacity + "x ")
					.append(rpmUnit);
			else
				level.append(stressCapacityLevels.get(index));

//			if (!isEngine && ((IRotate) block).showCapacityWithAnnotation())
//				level +=
//					" " + DARK_GRAY + TextFormatting.ITALIC + Lang.translate("tooltip.capacityProvided.asGenerator");

			list.add(Lang.translate("tooltip.capacityProvided")
				.withStyle(GRAY));
			list.add(level);

			IFormattableTextComponent genSpeed = generatorSpeed(block, rpmUnit);
			if (!genSpeed.getString()
				.isEmpty())
				list.add(new StringTextComponent(" ").append(genSpeed)
					.withStyle(DARK_GRAY));
		}

		// if (hasSpeedRequirement || hasStressImpact || hasStressCapacity)
		// add(linesOnShift, "");
		return list;
	}

	public static String makeProgressBar(int length, int filledLength) {
		String bar = " ";
		int emptySpaces = length - 1 - filledLength;
		for (int i = 0; i <= filledLength; i++)
			bar += "\u2588";
		for (int i = 0; i < emptySpaces; i++)
			bar += "\u2592";
		return bar + " ";
	}

	public ItemDescription withBehaviour(String condition, String behaviour) {
		add(linesOnShift, new StringTextComponent(condition).withStyle(GRAY));
		addStrings(linesOnShift, cutStringTextComponent(behaviour, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription withControl(String condition, String action) {
		add(linesOnCtrl, new StringTextComponent(condition).withStyle(GRAY));
		addStrings(linesOnCtrl, cutStringTextComponent(action, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription createTabs() {
		boolean hasDescription = !linesOnShift.isEmpty();
		boolean hasControls = !linesOnCtrl.isEmpty();

		if (hasDescription || hasControls) {
			String[] holdDesc = Lang.translate("tooltip.holdForDescription", "$").getString().split("\\$");
			String[] holdCtrl = Lang.translate("tooltip.holdForControls", "$").getString().split("\\$");
			IFormattableTextComponent keyShift = Lang.translate("tooltip.keyShift");
			IFormattableTextComponent keyCtrl = Lang.translate("tooltip.keyCtrl");
			for (List<ITextComponent> list : Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
				boolean shift = list == linesOnShift;
				boolean ctrl = list == linesOnCtrl;

				if (holdDesc.length != 2 || holdCtrl.length != 2) {
					list.add(0, new StringTextComponent("Invalid lang formatting!"));
					continue;
				}

				if (hasControls) {
					IFormattableTextComponent tabBuilder = new StringTextComponent("");
					tabBuilder.append(new StringTextComponent(holdCtrl[0]).withStyle(DARK_GRAY));
					tabBuilder.append(keyCtrl.plainCopy()
						.withStyle(ctrl ? WHITE : GRAY));
					tabBuilder.append(new StringTextComponent(holdCtrl[1]).withStyle(DARK_GRAY));
					list.add(0, tabBuilder);
				}

				if (hasDescription) {
					IFormattableTextComponent tabBuilder = new StringTextComponent("");
					tabBuilder.append(new StringTextComponent(holdDesc[0]).withStyle(DARK_GRAY));
					tabBuilder.append(keyShift.plainCopy()
						.withStyle(shift ? WHITE : GRAY));
					tabBuilder.append(new StringTextComponent(holdDesc[1]).withStyle(DARK_GRAY));
					list.add(0, tabBuilder);
				}

				if (shift || ctrl)
					list.add(hasDescription && hasControls ? 2 : 1, new StringTextComponent(""));
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

	public static void addStrings(List<ITextComponent> infoList, List<ITextComponent> textLines) {
		textLines.forEach(s -> add(infoList, s));
	}

	public static void add(List<ITextComponent> infoList, List<ITextComponent> textLines) {
		infoList.addAll(textLines);
	}

	public static void add(List<ITextComponent> infoList, ITextComponent line) {
		infoList.add(line);
	}

	public Palette getPalette() {
		return palette;
	}

	public List<ITextComponent> addInformation(List<ITextComponent> tooltip) {
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

	public List<ITextComponent> getLines() {
		return lines;
	}

	public List<ITextComponent> getLinesOnCtrl() {
		return linesOnCtrl;
	}

	public List<ITextComponent> getLinesOnShift() {
		return linesOnShift;
	}

	private static IFormattableTextComponent generatorSpeed(Block block, ITextComponent unitRPM) {
		String value = "";

		if (block instanceof WaterWheelBlock) {
			int baseSpeed = AllConfigs.SERVER.kinetics.waterWheelBaseSpeed.get();
			int speedmod = AllConfigs.SERVER.kinetics.waterWheelFlowSpeed.get();
			value = (speedmod + baseSpeed) + "-" + (baseSpeed + (speedmod * 3));
		}

		else if (block instanceof EncasedFanBlock)
			value = AllConfigs.SERVER.kinetics.generatingFanSpeed.get()
				.toString();

		else if (block instanceof FurnaceEngineBlock) {
			int baseSpeed = AllConfigs.SERVER.kinetics.furnaceEngineSpeed.get();
			value = baseSpeed + "-" + (baseSpeed * 2);
		}

		return !value.equals("") ? Lang.translate("tooltip.generationSpeed", value, unitRPM)
			: StringTextComponent.EMPTY.plainCopy();
	}

}
