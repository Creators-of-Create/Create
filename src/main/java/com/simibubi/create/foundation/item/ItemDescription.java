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
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;

public class ItemDescription {

	public static final ItemDescription MISSING = new ItemDescription(null);
	public static Component trim = Components.literal("                          ").withStyle(WHITE, STRIKETHROUGH);

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
		LangBuilder rpmUnit = Lang.translate("generic.unit.rpm");
		LangBuilder suUnit = Lang.translate("generic.unit.stress");

		boolean hasGoggles = GogglesItem.isWearingGoggles(Minecraft.getInstance().player);

		boolean showStressImpact;
		if (!(block instanceof IRotate)) {
			showStressImpact = true;
		} else {
			showStressImpact = !((IRotate) block).hideStressImpact();
		}

		boolean hasStressImpact =
			StressImpact.isEnabled() && showStressImpact && BlockStressValues.getImpact(block) > 0;
		boolean hasStressCapacity = StressImpact.isEnabled() && BlockStressValues.hasCapacity(block);

		if (hasStressImpact) {
			Lang.translate("tooltip.stressImpact")
				.style(GRAY)
				.addTo(list);

			double impact = BlockStressValues.getImpact(block);
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			LangBuilder builder = Lang.builder()
				.add(Lang.text(makeProgressBar(3, impactId.ordinal() + 1))
					.style(impactId.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(Lang.number(impact))
					.text("x ")
					.add(rpmUnit)
					.addTo(list);
			} else
				builder.translate("tooltip.stressImpact." + Lang.asId(impactId.name()))
					.addTo(list);
		}

		if (hasStressCapacity) {
			Lang.translate("tooltip.capacityProvided")
				.style(GRAY)
				.addTo(list);

			double capacity = BlockStressValues.getCapacity(block);
			BlockStressValues.IStressValueProvider stressProvider = BlockStressValues.getProvider(block);
			Couple<Integer> generatedRPM = stressProvider != null ?
				stressProvider.getGeneratedRPM(block)
                : null;

			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.HIGH
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			StressImpact opposite = StressImpact.values()[StressImpact.values().length - 2 - impactId.ordinal()];
			LangBuilder builder = Lang.builder()
				.add(Lang.text(makeProgressBar(3, impactId.ordinal() + 1))
					.style(opposite.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(Lang.number(capacity))
					.text("x ")
					.add(rpmUnit)
					.addTo(list);

				if (generatedRPM != null) {
					LangBuilder amount = Lang.number(capacity * generatedRPM.getSecond())
						.add(suUnit);
					Lang.text(" -> ")
						.add(!generatedRPM.getFirst()
							.equals(generatedRPM.getSecond()) ? Lang.translate("tooltip.up_to", amount) : amount)
						.style(DARK_GRAY)
						.addTo(list);
				}
			} else
				builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name()))
					.addTo(list);
		}

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
		add(linesOnShift, Components.literal(condition).withStyle(GRAY));
		addStrings(linesOnShift, cutStringTextComponent(behaviour, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription withControl(String condition, String action) {
		add(linesOnCtrl, Components.literal(condition).withStyle(GRAY));
		addStrings(linesOnCtrl, cutStringTextComponent(action, palette.color, palette.hColor, 1));
		return this;
	}

	public ItemDescription createTabs() {
		boolean hasDescription = !linesOnShift.isEmpty();
		boolean hasControls = !linesOnCtrl.isEmpty();

		if (hasDescription || hasControls) {
			String[] holdDesc = Lang.translateDirect("tooltip.holdForDescription", "$")
				.getString()
				.split("\\$");
			String[] holdCtrl = Lang.translateDirect("tooltip.holdForControls", "$")
				.getString()
				.split("\\$");
			MutableComponent keyShift = Lang.translateDirect("tooltip.keyShift");
			MutableComponent keyCtrl = Lang.translateDirect("tooltip.keyCtrl");
			for (List<Component> list : Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
				boolean shift = list == linesOnShift;
				boolean ctrl = list == linesOnCtrl;

				if (holdDesc.length != 2 || holdCtrl.length != 2) {
					list.add(0, Components.literal("Invalid lang formatting!"));
					continue;
				}

				if (hasControls) {
					MutableComponent tabBuilder = Components.empty();
					tabBuilder.append(Components.literal(holdCtrl[0]).withStyle(DARK_GRAY));
					tabBuilder.append(keyCtrl.plainCopy()
						.withStyle(ctrl ? WHITE : GRAY));
					tabBuilder.append(Components.literal(holdCtrl[1]).withStyle(DARK_GRAY));
					list.add(0, tabBuilder);
				}

				if (hasDescription) {
					MutableComponent tabBuilder = Components.empty();
					tabBuilder.append(Components.literal(holdDesc[0]).withStyle(DARK_GRAY));
					tabBuilder.append(keyShift.plainCopy()
						.withStyle(shift ? WHITE : GRAY));
					tabBuilder.append(Components.literal(holdDesc[1]).withStyle(DARK_GRAY));
					list.add(0, tabBuilder);
				}

				if (shift || ctrl)
					list.add(hasDescription && hasControls ? 2 : 1, Components.immutableEmpty());
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

}
