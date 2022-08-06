package com.simibubi.create.foundation.item;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Strings;
import com.mojang.bridge.game.Language;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.MinecraftForgeClient;

public class TooltipHelper {

	public static final int maxWidthPerLine = 200;
	public static final Map<String, ItemDescription> cachedTooltips = new HashMap<>();
	public static Language cachedLanguage;
	private static boolean gogglesMode;
	private static final Map<Item, Supplier<String>> tooltipReferrals = new HashMap<>();

	public static MutableComponent holdShift(Palette color, boolean highlighted) {
		return Lang.translateDirect("tooltip.holdForDescription", Lang.translateDirect("tooltip.keyShift")
			.withStyle(ChatFormatting.GRAY))
			.withStyle(ChatFormatting.DARK_GRAY);
	}

	public static void addHint(List<Component> tooltip, String hintKey, Object... messageParams) {
		Component spacing = IHaveGoggleInformation.componentSpacing;
		tooltip.add(spacing.plainCopy()
			.append(Lang.translateDirect(hintKey + ".title"))
			.withStyle(ChatFormatting.GOLD));
		Component hint = Lang.translateDirect(hintKey);
		List<Component> cutComponent = TooltipHelper.cutTextComponent(hint, ChatFormatting.GRAY, ChatFormatting.WHITE);
		for (Component component : cutComponent)
			tooltip.add(spacing.plainCopy()
				.append(component));
	}

	public static void referTo(ItemLike item, Supplier<? extends ItemLike> itemWithTooltip) {
		tooltipReferrals.put(item.asItem(), () -> itemWithTooltip.get()
			.asItem()
			.getDescriptionId());
	}

	public static void referTo(ItemLike item, String string) {
		tooltipReferrals.put(item.asItem(), () -> string);
	}

	@Deprecated
	public static List<String> cutString(Component s, ChatFormatting defaultColor, ChatFormatting highlightColor) {
		return cutString(s.getString(), defaultColor, highlightColor, 0);
	}

	@Deprecated
	public static List<String> cutString(String s, ChatFormatting defaultColor, ChatFormatting highlightColor,
		int indent) {
		// Apply markup
		String markedUp = s.replaceAll("_([^_]+)_", highlightColor + "$1" + defaultColor);

		// Split words
		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(MinecraftForgeClient.getLocale());
		iterator.setText(markedUp);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = markedUp.substring(start, end);
			words.add(word);
		}

		Font font = Minecraft.getInstance().font;
		List<String> lines = FontHelper.cutString(font, markedUp, maxWidthPerLine);

		// Format
		String lineStart = Strings.repeat(" ", indent);
		List<String> formattedLines = new ArrayList<>(lines.size());
		String format = defaultColor.toString();
		for (String line : lines) {
			String formattedLine = format + lineStart + line;
			formattedLines.add(formattedLine);
//			format = TextFormatting.getFormatString(formattedLine);
		}
		return formattedLines;
	}

	public static List<Component> cutStringTextComponent(String c, ChatFormatting defaultColor,
		ChatFormatting highlightColor) {
		return cutTextComponent(Components.literal(c), defaultColor, highlightColor, 0);
	}

	public static List<Component> cutTextComponent(Component c, ChatFormatting defaultColor,
		ChatFormatting highlightColor) {
		return cutTextComponent(c, defaultColor, highlightColor, 0);
	}

	public static List<Component> cutStringTextComponent(String c, ChatFormatting defaultColor,
		ChatFormatting highlightColor, int indent) {
		return cutTextComponent(Components.literal(c), defaultColor, highlightColor, indent);
	}

	public static List<Component> cutTextComponent(Component c, ChatFormatting defaultColor,
		ChatFormatting highlightColor, int indent) {
		String s = c.getString();

		// Apply markup
		String markedUp = s;// .replaceAll("_([^_]+)_", highlightColor + "$1" + defaultColor);

		// Split words
		List<String> words = new LinkedList<>();
		BreakIterator iterator = BreakIterator.getLineInstance(MinecraftForgeClient.getLocale());
		iterator.setText(markedUp);
		int start = iterator.first();
		for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
			String word = markedUp.substring(start, end);
			words.add(word);
		}

		// Apply hard wrap
		Font font = Minecraft.getInstance().font;
		List<String> lines = new LinkedList<>();
		StringBuilder currentLine = new StringBuilder();
		int width = 0;
		for (String word : words) {
			int newWidth = font.width(word.replaceAll("_", ""));
			if (width + newWidth > maxWidthPerLine) {
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
		lineStart.withStyle(defaultColor);
		List<Component> formattedLines = new ArrayList<>(lines.size());
		Couple<ChatFormatting> f = Couple.create(highlightColor, defaultColor);

		boolean currentlyHighlighted = false;
		for (String string : lines) {
			MutableComponent currentComponent = lineStart.plainCopy();
			String[] split = string.split("_");
			for (String part : split) {
				currentComponent.append(Components.literal(part).withStyle(f.get(currentlyHighlighted)));
				currentlyHighlighted = !currentlyHighlighted;
			}

			formattedLines.add(currentComponent);
			currentlyHighlighted = !currentlyHighlighted;
		}

		return formattedLines;
	}

//	public static List<ITextComponent> cutTextComponentOld(ITextComponent c, TextFormatting defaultColor,
//		TextFormatting highlightColor, int indent) {
//		IFormattableTextComponent lineStart = StringTextComponent.EMPTY.copy();
//		for (int i = 0; i < indent; i++)
//			lineStart.append(" ");
//		lineStart.formatted(defaultColor);
//
//		List<ITextComponent> lines = new ArrayList<>();
//		String rawText = getUnformattedDeepText(c);
//		String[] words = rawText.split(" ");
//		String word;
//		IFormattableTextComponent currentLine = lineStart.copy();
//
//		boolean firstWord = true;
//		boolean lastWord;
//
//		// Apply hard wrap
//		for (int i = 0; i < words.length; i++) {
//			word = words[i];
//			lastWord = i == words.length - 1;
//
//			if (!lastWord && !firstWord && getComponentLength(currentLine) + word.length() > maxCharsPerLine) {
//				lines.add(currentLine);
//				currentLine = lineStart.copy();
//				firstWord = true;
//			}
//
//			currentLine.append(new StringTextComponent((firstWord ? "" : " ") + word.replace("_", ""))
//				.formatted(word.matches("_([^_]+)_") ? highlightColor : defaultColor));
//			firstWord = false;
//		}
//
//		if (!firstWord) {
//			lines.add(currentLine);
//		}
//
//		return lines;
//	}

	private static void checkLocale() {
		Language currentLanguage = Minecraft.getInstance()
			.getLanguageManager()
			.getSelected();
		if (cachedLanguage != currentLanguage) {
			cachedTooltips.clear();
			cachedLanguage = currentLanguage;
		}
	}

	public static boolean hasTooltip(ItemStack stack, Player player) {
		checkLocale();

		boolean hasGoggles = GogglesItem.isWearingGoggles(player);

		if (hasGoggles != gogglesMode) {
			gogglesMode = hasGoggles;
			cachedTooltips.clear();
		}

		String key = getTooltipTranslationKey(stack);
		if (cachedTooltips.containsKey(key))
			return cachedTooltips.get(key) != ItemDescription.MISSING;
		return findTooltip(stack);
	}

	public static ItemDescription getTooltip(ItemStack stack) {
		checkLocale();
		String key = getTooltipTranslationKey(stack);
		if (cachedTooltips.containsKey(key)) {
			ItemDescription itemDescription = cachedTooltips.get(key);
			if (itemDescription != ItemDescription.MISSING)
				return itemDescription;
		}
		return null;
	}

	private static boolean findTooltip(ItemStack stack) {
		String key = getTooltipTranslationKey(stack);
		if (I18n.exists(key)) {
			cachedTooltips.put(key, buildToolTip(key, stack));
			return true;
		}
		cachedTooltips.put(key, ItemDescription.MISSING);
		return false;
	}

	private static ItemDescription buildToolTip(String translationKey, ItemStack stack) {
		AllSections module = AllSections.of(stack);
		ItemDescription tooltip = new ItemDescription(module.getTooltipPalette());
		String summaryKey = translationKey + ".summary";

		// Summary
		if (I18n.exists(summaryKey))
			tooltip = tooltip.withSummary(Components.literal(I18n.get(summaryKey)));

		// Requirements
//		if (stack.getItem() instanceof BlockItem) {
//			BlockItem item = (BlockItem) stack.getItem();
//			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof EngineBlock) {
//				tooltip = tooltip.withKineticStats(item.getBlock());
//			}
//		}

		// Behaviours
		for (int i = 1; i < 100; i++) {
			String conditionKey = translationKey + ".condition" + i;
			String behaviourKey = translationKey + ".behaviour" + i;
			if (!I18n.exists(conditionKey))
				break;
			if (i == 1)
				tooltip.getLinesOnShift()
					.add(Components.immutableEmpty());
			tooltip.withBehaviour(I18n.get(conditionKey), I18n.get(behaviourKey));
		}

		// Controls
		for (int i = 1; i < 100; i++) {
			String controlKey = translationKey + ".control" + i;
			String actionKey = translationKey + ".action" + i;
			if (!I18n.exists(controlKey))
				break;
			tooltip.withControl(I18n.get(controlKey), I18n.get(actionKey));
		}

		return tooltip.createTabs();
	}

	public static String getTooltipTranslationKey(ItemStack stack) {
		Item item = stack.getItem();
		if (tooltipReferrals.containsKey(item))
			return tooltipReferrals.get(item)
				.get() + ".tooltip";
		return item.getDescriptionId(stack) + ".tooltip";
	}

//	private static int getComponentLength(ITextComponent component) {
//		AtomicInteger l = new AtomicInteger();
//		TextProcessing.visitFormatted(component, Style.EMPTY, (s, style, charConsumer) -> {
//			l.getAndIncrement();
//			return true;
//		});
//		return l.get();
//	}

}
