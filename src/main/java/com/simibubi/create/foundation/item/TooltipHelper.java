package com.simibubi.create.foundation.item;

import static net.minecraft.util.text.TextFormatting.DARK_GRAY;
import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Strings;
import com.mojang.bridge.game.Language;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.FontHelper;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.MinecraftForgeClient;

public class TooltipHelper {

	public static final int maxWidthPerLine = 200;
	public static final Map<String, ItemDescription> cachedTooltips = new HashMap<>();
	public static Language cachedLanguage;
	private static boolean gogglesMode;
	private static final Map<Item, Supplier<String>> tooltipReferrals = new HashMap<>();

	public static String holdShift(Palette color, boolean highlighted) {
		TextFormatting colorFormat = highlighted ? color.hColor : color.color;
		return DARK_GRAY
			+ Lang.translate("tooltip.holdKey", colorFormat + Lang.translate("tooltip.keyShift") + DARK_GRAY);
	}

	public static void addHint(List<String> tooltip, String hintKey, Object... messageParams) {
		String spacing = IHaveGoggleInformation.spacing;
		tooltip.add(spacing + GOLD + Lang.translate(hintKey + ".title"));
		String hint = Lang.translate(hintKey);
		List<String> cutString = TooltipHelper.cutString(spacing + hint, GRAY, TextFormatting.WHITE);
		for (int i = 0; i < cutString.size(); i++)
			tooltip.add((i == 0 ? "" : spacing) + cutString.get(i));
	}
	
	public static void referTo(IItemProvider item, Supplier<? extends IItemProvider> itemWithTooltip) {
		tooltipReferrals.put(item.asItem(), () -> itemWithTooltip.get()
			.asItem()
			.getTranslationKey());
	}
	
	public static void referTo(IItemProvider item, String string) {
		tooltipReferrals.put(item.asItem(), () -> string);
	}

	public static List<String> cutString(String s, TextFormatting defaultColor, TextFormatting highlightColor) {
		return cutString(s, defaultColor, highlightColor, 0);
	}

	public static List<String> cutString(String s, TextFormatting defaultColor, TextFormatting highlightColor,
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

		FontRenderer font = Minecraft.getInstance().fontRenderer;
		List<String> lines = FontHelper.cutString(font, markedUp, maxWidthPerLine);

		// Format
		String lineStart = Strings.repeat(" ", indent);
		List<String> formattedLines = new ArrayList<>(lines.size());
		String format = defaultColor.toString();
		for (String line : lines) {
			String formattedLine = format + lineStart + line;
			formattedLines.add(formattedLine);
			format = TextFormatting.getFormatString(formattedLine);
		}
		return formattedLines;
	}

	private static void checkLocale() {
		Language currentLanguage = Minecraft.getInstance()
			.getLanguageManager()
			.getCurrentLanguage();
		if (cachedLanguage != currentLanguage) {
			cachedTooltips.clear();
			cachedLanguage = currentLanguage;
		}
	}

	public static boolean hasTooltip(ItemStack stack, PlayerEntity player) {
		checkLocale();

		boolean hasGlasses = AllItems.GOGGLES.isIn(player.getItemStackFromSlot(EquipmentSlotType.HEAD));

		if (hasGlasses != gogglesMode) {
			gogglesMode = hasGlasses;
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
		if (I18n.hasKey(key)) {
			cachedTooltips.put(key, buildToolTip(key, stack));
			return true;
		}
		cachedTooltips.put(key, ItemDescription.MISSING);
		return false;
	}

	private static ItemDescription buildToolTip(String translationKey, ItemStack stack) {
		AllSections module = AllSections.of(stack);
		if (I18n.format(translationKey)
			.equals("WIP"))
			return new WipScription(module.getTooltipPalette());

		ItemDescription tooltip = new ItemDescription(module.getTooltipPalette());
		String summaryKey = translationKey + ".summary";

		// Summary
		if (I18n.hasKey(summaryKey))
			tooltip = tooltip.withSummary(I18n.format(summaryKey));

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
			if (!I18n.hasKey(conditionKey))
				break;
			if (i == 1)
				tooltip.getLinesOnShift().add(new StringTextComponent(""));
			tooltip.withBehaviour(I18n.format(conditionKey), I18n.format(behaviourKey));
		}

		// Controls
		for (int i = 1; i < 100; i++) {
			String controlKey = translationKey + ".control" + i;
			String actionKey = translationKey + ".action" + i;
			if (!I18n.hasKey(controlKey))
				break;
			tooltip.withControl(I18n.format(controlKey), I18n.format(actionKey));
		}

		return tooltip.createTabs();
	}

	public static String getTooltipTranslationKey(ItemStack stack) {
		Item item = stack.getItem();
		if (tooltipReferrals.containsKey(item))
			return tooltipReferrals.get(item).get() + ".tooltip";
		return item.getTranslationKey(stack) + ".tooltip";
	}

}
