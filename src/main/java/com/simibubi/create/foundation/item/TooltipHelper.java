package com.simibubi.create.foundation.item;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Strings;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TooltipHelper {

	public static final int MAX_WIDTH_PER_LINE = 200;

	public static MutableComponent holdShift(Palette palette, boolean highlighted) {
		return Lang.translateDirect("tooltip.holdForDescription", Lang.translateDirect("tooltip.keyShift")
			.withStyle(ChatFormatting.GRAY))
			.withStyle(ChatFormatting.DARK_GRAY);
	}

	public static void addHint(List<Component> tooltip, String hintKey, Object... messageParams) {
		Lang.translate(hintKey + ".title").style(ChatFormatting.GOLD).forGoggles(tooltip);
		Component hint = Lang.translateDirect(hintKey);
		List<Component> cutComponent = cutTextComponent(hint, Palette.GRAY_AND_WHITE);
		for (Component component : cutComponent)
			Lang.builder().add(component).forGoggles(tooltip);
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

	public static Style styleFromColor(ChatFormatting color) {
		return Style.EMPTY.applyFormat(color);
	}

	public static Style styleFromColor(int hex) {
		return Style.EMPTY.withColor(hex);
	}

	public static List<Component> cutStringTextComponent(String s, Palette palette) {
		return cutTextComponent(Components.literal(s), palette);
	}

	public static List<Component> cutTextComponent(Component c, Palette palette) {
		return cutTextComponent(c, palette.primary(), palette.highlight());
	}

	public static List<Component> cutStringTextComponent(String s, Style primaryStyle,
		Style highlightStyle) {
		return cutTextComponent(Components.literal(s), primaryStyle, highlightStyle);
	}

	public static List<Component> cutTextComponent(Component c, Style primaryStyle,
		Style highlightStyle) {
		return cutTextComponent(c, primaryStyle, highlightStyle, 0);
	}

	public static List<Component> cutStringTextComponent(String c, Style primaryStyle,
		Style highlightStyle, int indent) {
		return cutTextComponent(Components.literal(c), primaryStyle, highlightStyle, indent);
	}

	public static List<Component> cutTextComponent(Component c, Style primaryStyle,
		Style highlightStyle, int indent) {
		String s = c.getString();

		// Split words
		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(Minecraft.getInstance().getLocale());
		iterator.setText(s);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = s.substring(start, end);
			words.add(word);
		}

		// Apply hard wrap
		Font font = Minecraft.getInstance().font;
		List<String> lines = new LinkedList<>();
		StringBuilder currentLine = new StringBuilder();
		int width = 0;
		for (String word : words) {
			int newWidth = font.width(word.replaceAll("_", ""));
			if (width + newWidth > MAX_WIDTH_PER_LINE) {
				if (width > 0) {
					String line = currentLine.toString();
					lines.add(line);
					currentLine = new StringBuilder();
					width = 0;
				} else {
					lines.add(word);
					continue;
				}
			}
			currentLine.append(word);
			width += newWidth;
		}
		if (width > 0) {
			lines.add(currentLine.toString());
		}

		// Format
		MutableComponent lineStart = Components.literal(Strings.repeat(" ", indent));
		lineStart.withStyle(primaryStyle);
		List<Component> formattedLines = new ArrayList<>(lines.size());
		Couple<Style> styles = Couple.create(highlightStyle, primaryStyle);

		boolean currentlyHighlighted = false;
		for (String string : lines) {
			MutableComponent currentComponent = lineStart.plainCopy();
			String[] split = string.split("_");
			for (String part : split) {
				currentComponent.append(Components.literal(part).withStyle(styles.get(currentlyHighlighted)));
				currentlyHighlighted = !currentlyHighlighted;
			}

			formattedLines.add(currentComponent);
			currentlyHighlighted = !currentlyHighlighted;
		}

		return formattedLines;
	}

	public record Palette(Style primary, Style highlight) {
		public static final Palette STANDARD_CREATE = new Palette(styleFromColor(0xC9974C), styleFromColor(0xF1DD79));

		public static final Palette BLUE = ofColors(ChatFormatting.BLUE, ChatFormatting.AQUA);
		public static final Palette GREEN = ofColors(ChatFormatting.DARK_GREEN, ChatFormatting.GREEN);
		public static final Palette YELLOW = ofColors(ChatFormatting.GOLD, ChatFormatting.YELLOW);
		public static final Palette RED = ofColors(ChatFormatting.DARK_RED, ChatFormatting.RED);
		public static final Palette PURPLE = ofColors(ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
		public static final Palette GRAY = ofColors(ChatFormatting.DARK_GRAY, ChatFormatting.GRAY);

		public static final Palette ALL_GRAY = ofColors(ChatFormatting.GRAY, ChatFormatting.GRAY);
		public static final Palette GRAY_AND_BLUE = ofColors(ChatFormatting.GRAY, ChatFormatting.BLUE);
		public static final Palette GRAY_AND_WHITE = ofColors(ChatFormatting.GRAY, ChatFormatting.WHITE);
		public static final Palette GRAY_AND_GOLD = ofColors(ChatFormatting.GRAY, ChatFormatting.GOLD);
		public static final Palette GRAY_AND_RED = ofColors(ChatFormatting.GRAY, ChatFormatting.RED);

		public static Palette ofColors(ChatFormatting primary, ChatFormatting highlight) {
			return new Palette(styleFromColor(primary), styleFromColor(highlight));
		}
	}

}
