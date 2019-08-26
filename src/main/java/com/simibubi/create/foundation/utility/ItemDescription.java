package com.simibubi.create.foundation.utility;

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

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemDescription {

	public static final int maxCharsPerLine = 35;
	public static ITextComponent trim = new StringTextComponent(
			WHITE + "" + STRIKETHROUGH + "                          ");

	public enum Palette {

		Blue(BLUE, AQUA), Green(DARK_GREEN, GREEN), Yellow(GOLD, YELLOW), Red(DARK_RED, RED),
		Purple(DARK_PURPLE, LIGHT_PURPLE),

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
		trim = new StringTextComponent(WHITE + "" + STRIKETHROUGH + "                                        ");
	}

	public ItemDescription withSummary(String summary) {
		add(linesOnShift, cutString(summary, palette.color));
		linesOnShift.add(trim);
		return this;
	}

	public ItemDescription withBehaviour(String condition, String behaviour) {
		add(linesOnShift, GRAY + condition);
		add(linesOnShift, cutString(behaviour, palette.color, 1));
		return this;
	}

	public ItemDescription withControl(String condition, String action) {
		add(linesOnCtrl, GRAY + condition);
		add(linesOnCtrl, cutString(action, palette.color, 1));
		return this;
	}

	public ItemDescription createTabs() {
		boolean hasDescription = !linesOnShift.isEmpty();
		boolean hasControls = !linesOnCtrl.isEmpty();

		if (hasDescription || hasControls) {
			for (List<ITextComponent> list : Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
				boolean shift = list == linesOnShift;
				boolean ctrl = list == linesOnCtrl;

				String tabs = DARK_GRAY + "Hold ";
				if (hasDescription)
					tabs += "<" + (shift ? palette.hColor : palette.color) + "Shift" + DARK_GRAY + ">";
				if (hasDescription && hasControls)
					tabs += " or ";
				if (hasControls)
					tabs += "<" + (ctrl ? palette.hColor : palette.color) + "Control" + DARK_GRAY + ">";

				list.add(0, new StringTextComponent(tabs));
				if (shift || ctrl)
					list.add(1, trim);
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

	public List<String> cutString(String s, TextFormatting defaultColor) {
		return cutString(s, defaultColor, 0);
	}

	public List<String> cutString(String s, TextFormatting defaultColor, int indent) {

		String lineStart = defaultColor.toString();
		for (int i = 0; i < indent; i++)
			lineStart += " ";

		String[] words = s.split(" ");
		List<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder(lineStart);
		boolean firstWord = true;

		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			boolean lastWord = i == words.length - 1;

			if (!lastWord && !firstWord && currentLine.length() + word.length() > maxCharsPerLine) {
				lines.add(currentLine.toString());
				currentLine = new StringBuilder(lineStart);
				firstWord = true;
			}

			currentLine.append((firstWord ? "" : " ") + word);
			firstWord = false;
		}

		if (!firstWord) {
			lines.add(currentLine.toString());
		}

		return lines;
	}

	public static void add(List<ITextComponent> infoList, List<String> textLines) {
		textLines.forEach(s -> add(infoList, s));
	}

	public static void add(List<ITextComponent> infoList, String line) {
		infoList.add(new StringTextComponent(line));
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

}
