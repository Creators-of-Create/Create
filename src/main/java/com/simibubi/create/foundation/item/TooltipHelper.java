package com.simibubi.create.foundation.item;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Strings;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TooltipHelper {

	public static final int MAX_WIDTH_PER_LINE = 200;

	public static MutableComponent holdShift(FontHelper.Palette palette, boolean highlighted) {
		return CreateLang.translateDirect("tooltip.holdForDescription", CreateLang.translateDirect("tooltip.keyShift")
			.withStyle(ChatFormatting.GRAY))
			.withStyle(ChatFormatting.DARK_GRAY);
	}

	public static void addHint(List<Component> tooltip, String hintKey, Object... messageParams) {
		Component spacing = IHaveGoggleInformation.componentSpacing;
		tooltip.add(spacing.plainCopy()
			.append(CreateLang.translateDirect(hintKey + ".title"))
			.withStyle(ChatFormatting.GOLD));
		Component hint = CreateLang.translateDirect(hintKey);
		List<Component> cutComponent = cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
		for (Component component : cutComponent)
			tooltip.add(spacing.plainCopy()
				.append(component));
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

	public static List<Component> cutStringTextComponent(String s, FontHelper.Palette palette) {
		return cutTextComponent(Components.literal(s), palette);
	}

	public static List<Component> cutTextComponent(Component c, FontHelper.Palette palette) {
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

}
