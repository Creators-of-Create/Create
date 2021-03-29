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
		return this;
	}

	public static List<ITextComponent> getKineticStats(Block block) {
		List<ITextComponent> list = new ArrayList<>();

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
			IFormattableTextComponent level = new StringTextComponent(makeProgressBar(3, index)).formatted(minimumRequiredSpeedLevel.getTextColor());

			if (hasGlasses)
				level.append(String.valueOf(minimumRequiredSpeedLevel.getSpeedValue())).append(rpmUnit).append("+");
			else
				level.append(speedLevels.get(index));

			list.add(Lang.translate("tooltip.speedRequirement").formatted(GRAY));
			list.add(level);
		}

		if (hasStressImpact && !(!isEngine && ((IRotate) block).hideStressImpact())) {
			List<ITextComponent> stressLevels = Lang.translatedOptions("tooltip.stressImpact", "low", "medium", "high");
			double impact = impacts.get(id)
				.get();
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			int index = impactId.ordinal();
			IFormattableTextComponent level = new StringTextComponent(makeProgressBar(3, index)).formatted(impactId.getAbsoluteColor());

			if (hasGlasses)
				level.append(impacts.get(id)
					.get() + "x ").append(rpmUnit);
			else
				level.append(stressLevels.get(index));

			list.add(Lang.translate("tooltip.stressImpact").formatted(GRAY));
			list.add(level);
		}

		if (hasStressCapacity) {
			List<ITextComponent> stressCapacityLevels =
				Lang.translatedOptions("tooltip.capacityProvided", "low", "medium", "high");
			double capacity = capacities.get(id)
				.get();
			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.LOW
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.HIGH);
			int index = StressImpact.values().length - 2 - impactId.ordinal();
			IFormattableTextComponent level = new StringTextComponent(makeProgressBar(3, index)).formatted(impactId.getAbsoluteColor());

			if (hasGlasses)
				level.append(capacity + "x ").append(rpmUnit);
			else
				level.append(stressCapacityLevels.get(index));
			
//			if (!isEngine && ((IRotate) block).showCapacityWithAnnotation())
//				level +=
//					" " + DARK_GRAY + TextFormatting.ITALIC + Lang.translate("tooltip.capacityProvided.asGenerator");

			list.add(Lang.translate("tooltip.capacityProvided").formatted(GRAY));
			list.add(level);

			IFormattableTextComponent genSpeed = generatorSpeed(block, rpmUnit);
			if (!genSpeed.equals(""))
				list.add(new StringTextComponent(" ").append(genSpeed).formatted(DARK_GRAY));
		}

		//		if (hasSpeedRequirement || hasStressImpact || hasStressCapacity)
		//			add(linesOnShift, "");
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
			String[] holdDesc = TooltipHelper.getUnformattedDeepText(Lang.translate("tooltip.holdForDescription", "$"))
				.split("\\$");
			String[] holdCtrl = TooltipHelper.getUnformattedDeepText(Lang.translate("tooltip.holdForControls", "$"))
				.split("\\$");
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
					tabBuilder.append(new StringTextComponent(holdCtrl[0]).formatted(DARK_GRAY));
					tabBuilder.append(keyCtrl.formatted(ctrl? WHITE : GRAY));
					tabBuilder.append(new StringTextComponent(holdCtrl[1]).formatted(DARK_GRAY));
					list.add(0, tabBuilder);
				}
				
				if (hasDescription) {
					IFormattableTextComponent tabBuilder = new StringTextComponent("");
					tabBuilder.append(new StringTextComponent(holdDesc[0]).formatted(DARK_GRAY));
					tabBuilder.append(keyShift.formatted(shift? WHITE : GRAY));
					tabBuilder.append(new StringTextComponent(holdDesc[1]).formatted(DARK_GRAY));
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

		return !value.equals("") ? Lang.translate("tooltip.generationSpeed", value, unitRPM) : StringTextComponent.EMPTY.copy();
	}

}
