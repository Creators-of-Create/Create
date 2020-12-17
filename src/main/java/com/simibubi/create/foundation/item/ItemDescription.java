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
import static net.minecraft.util.text.TextFormatting.ITALIC;
import static net.minecraft.util.text.TextFormatting.LIGHT_PURPLE;
import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.STRIKETHROUGH;
import static net.minecraft.util.text.TextFormatting.WHITE;
import static net.minecraft.util.text.TextFormatting.YELLOW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.components.fan.EncasedFanBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.components.flywheel.engine.FurnaceEngineBlock;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ItemDescription {

	public static final ItemDescription MISSING = new ItemDescription(null);
	public static ITextComponent trim =
		new StringTextComponent("                          ").formatted(WHITE, STRIKETHROUGH);

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
		add(linesOnShift, StringTextComponent.EMPTY);
		return this;
	}

	public ItemDescription withKineticStats(Block block) {

		boolean isEngine = block instanceof EngineBlock;
		CKinetics config = AllConfigs.SERVER.kinetics;
		SpeedLevel minimumRequiredSpeedLevel =
			isEngine ? SpeedLevel.NONE : ((IRotate) block).getMinimumRequiredSpeedLevel();
		boolean hasSpeedRequirement = minimumRequiredSpeedLevel != SpeedLevel.NONE;
		ResourceLocation id = block.getRegistryName();
		Map<ResourceLocation, ConfigValue<Double>> impacts = config.stressValues.getImpacts();
		Map<ResourceLocation, ConfigValue<Double>> capacities = config.stressValues.getCapacities();
		boolean hasStressImpact = impacts.containsKey(id) && impacts.get(id)
			.get() > 0 && StressImpact.isEnabled();
		boolean hasStressCapacity = capacities.containsKey(id) && StressImpact.isEnabled();
		boolean hasGlasses =
			AllItems.GOGGLES.get() == Minecraft.getInstance().player.getItemStackFromSlot(EquipmentSlotType.HEAD)
				.getItem();

		ITextComponent rpmUnit = Lang.translate("generic.unit.rpm");
		if (hasSpeedRequirement) {
			List<ITextComponent> speedLevels = Lang.translatedOptions("tooltip.speedRequirement", "none", "medium", "high");
			int index = minimumRequiredSpeedLevel.ordinal();
			IFormattableTextComponent level = new StringTextComponent(makeProgressBar(3, index)).append(speedLevels.get(index)).formatted(minimumRequiredSpeedLevel.getTextColor());

			if (hasGlasses)
				level.append(" (" + minimumRequiredSpeedLevel.getSpeedValue()).append(rpmUnit).append("+)");

			add(linesOnShift, Lang.translate("tooltip.speedRequirement").formatted(GRAY));
			add(linesOnShift, level);
		}

		if (hasStressImpact && !(!isEngine && ((IRotate) block).hideStressImpact())) {
			List<ITextComponent> stressLevels = Lang.translatedOptions("tooltip.stressImpact", "low", "medium", "high");
			double impact = impacts.get(id)
				.get();
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			int index = impactId.ordinal();
			IFormattableTextComponent level = new StringTextComponent(makeProgressBar(3, index)).append(stressLevels.get(index)).formatted(impactId.getAbsoluteColor());

			if (hasGlasses)
				level.append(" (" + impacts.get(id).get()).append("x ").append(rpmUnit).append(")");

			add(linesOnShift, Lang.translate("tooltip.stressImpact").formatted(GRAY));
			add(linesOnShift, level);
		}

		if (hasStressCapacity) {
			List<ITextComponent> stressCapacityLevels =
				Lang.translatedOptions("tooltip.capacityProvided", "low", "medium", "high");
			double capacity = capacities.get(id)
				.get();
			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.LOW
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.HIGH);
			int index = StressImpact.values().length - 2 - impactId.ordinal();
			IFormattableTextComponent level = new StringTextComponent(makeProgressBar(3, index)).append(stressCapacityLevels.get(index)).formatted(impactId.getAbsoluteColor());

			if (hasGlasses)
				level.append(" (" + capacity).append("x ").append(rpmUnit).append(")");
			if (!isEngine && ((IRotate) block).showCapacityWithAnnotation())
				level.append(" ").append(Lang.translate("tooltip.capacityProvided.asGenerator").formatted(DARK_GRAY, ITALIC));

			add(linesOnShift, Lang.translate("tooltip.capacityProvided").formatted(GRAY));
			add(linesOnShift, level);

			IFormattableTextComponent genSpeed = generatorSpeed(block, rpmUnit);
			if (!genSpeed.getUnformattedComponentText().equals("")) {
				add(linesOnShift, new StringTextComponent(" ").append(genSpeed).formatted(GREEN));
			}
		}

		if (hasSpeedRequirement || hasStressImpact || hasStressCapacity)
			add(linesOnShift, StringTextComponent.EMPTY);
		return this;
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
		add(linesOnShift, new StringTextComponent(condition).formatted(GRAY));
		addStrings(linesOnShift, cutStringTextComponent(behaviour, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription withControl(String condition, String action) {
		add(linesOnCtrl, new StringTextComponent(condition).formatted(GRAY));
		addStrings(linesOnCtrl, cutStringTextComponent(action, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription createTabs() {
		boolean hasDescription = !linesOnShift.isEmpty();
		boolean hasControls = !linesOnCtrl.isEmpty();

		if (hasDescription || hasControls) {
			String[] holdKey = TooltipHelper.getUnformattedDeepText(Lang.translate("tooltip.holdKey", "$"))
				.split("\\$");
			String[] holdKeyOrKey = TooltipHelper.getUnformattedDeepText(Lang.translate("tooltip.holdKeyOrKey", "$", "$"))
				.split("\\$");
			ITextComponent keyShift = Lang.translate("tooltip.keyShift");
			ITextComponent keyCtrl = Lang.translate("tooltip.keyCtrl");
			for (List<ITextComponent> list : Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
				boolean shift = list == linesOnShift;
				boolean ctrl = list == linesOnCtrl;

				if (holdKey.length != 2 || holdKeyOrKey.length != 3) {
					list.add(0, new StringTextComponent("Invalid lang formatting!"));
					continue;
				}

				IFormattableTextComponent tabBuilder = StringTextComponent.EMPTY.copy();
				if (hasDescription && hasControls) {
					tabBuilder.append(holdKeyOrKey[0]);
					tabBuilder.append(keyShift.copy().formatted(shift ? palette.hColor : palette.color));
					tabBuilder.append(holdKeyOrKey[1]);
					tabBuilder.append(keyCtrl.copy().formatted(ctrl ? palette.hColor : palette.color));
					tabBuilder.append(holdKeyOrKey[2]);

				} else {
					tabBuilder.append(holdKey[0]);
					tabBuilder.append((hasDescription ? keyShift : keyCtrl).copy().formatted((hasDescription ? shift : ctrl) ? palette.hColor : palette.color));
					tabBuilder.append(holdKey[1]);
				}
				tabBuilder.formatted(DARK_GRAY);
				list.add(0, tabBuilder);
				if (shift || ctrl)
					list.add(1, StringTextComponent.EMPTY);
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

	private IFormattableTextComponent generatorSpeed(Block block, ITextComponent unitRPM) {
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

		return !value.equals("") ? Lang.translate("tooltip.generationSpeed", value, unitRPM) : StringTextComponent.EMPTY.copy();
	}

}
