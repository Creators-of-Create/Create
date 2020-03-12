package com.simibubi.create.foundation.item;

import static net.minecraft.util.text.TextFormatting.DARK_GRAY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.bridge.game.Language;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.modules.curiosities.tools.AllToolTiers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class TooltipHelper {

	public static final int maxCharsPerLine = 35;
	public static final Map<String, ItemDescription> cachedTooltips = new HashMap<>();
	public static Language cachedLanguage;
	private static boolean gogglesMode;

	public static String holdShift(Palette color, boolean highlighted) {
		TextFormatting colorFormat = highlighted ? color.hColor : color.color;
		return DARK_GRAY
				+ Lang.translate("tooltip.holdKey", colorFormat + Lang.translate("tooltip.keyShift") + DARK_GRAY);
	}

	public static List<String> cutString(String s, TextFormatting defaultColor, TextFormatting highlightColor) {
		return cutString(s, defaultColor, highlightColor, 0);
	}

	public static List<String> cutString(String s, TextFormatting defaultColor, TextFormatting highlightColor,
			int indent) {
		String lineStart = defaultColor.toString();
		for (int i = 0; i < indent; i++)
			lineStart += " ";

		String[] words = s.split(" ");
		List<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder(lineStart);
		boolean firstWord = true;

		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word.matches("_.+_.?"))
				word = highlightColor + word.replaceAll("\\_", "") + defaultColor;

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

	private static void checkLocale() {
		Language currentLanguage = Minecraft.getInstance().getLanguageManager().getCurrentLanguage();
		if (cachedLanguage != currentLanguage) {
			cachedTooltips.clear();
			cachedLanguage = currentLanguage;
		}
	}

	public static boolean hasTooltip(ItemStack stack) {
		checkLocale();

		boolean hasGlasses =
			AllItems.GOGGLES.typeOf(Minecraft.getInstance().player.getItemStackFromSlot(EquipmentSlotType.HEAD));
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
		IModule module = IModule.of(stack);
		if (I18n.format(translationKey).equals("WIP"))
			return new WipScription(module.getToolTipColor());

		ItemDescription tooltip = new ItemDescription(module.getToolTipColor());
		String summaryKey = translationKey + ".summary";

		// Summary
		if (I18n.hasKey(summaryKey))
			tooltip = tooltip.withSummary(I18n.format(summaryKey));

		// Requirements
		if (stack.getItem() instanceof BlockItem) {
			BlockItem item = (BlockItem) stack.getItem();
			if (item.getBlock() instanceof IRotate || item.getBlock() instanceof EngineBlock) {
				tooltip = tooltip.withKineticStats(item.getBlock());
			}
		}

		// Behaviours
		for (int i = 1; i < 100; i++) {
			String conditionKey = translationKey + ".condition" + i;
			String behaviourKey = translationKey + ".behaviour" + i;
			if (!I18n.hasKey(conditionKey))
				break;
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

		if (stack.getItem() instanceof AbstractToolItem) {
			AbstractToolItem abstractToolItem = (AbstractToolItem) stack.getItem();
			if (abstractToolItem.getTier() instanceof AllToolTiers) {
				AllToolTiers allToolTiers = (AllToolTiers) abstractToolItem.getTier();
				return "tool.create." + Lang.asId(allToolTiers.name()) + ".tooltip";
			}
		}

		return stack.getItem().getTranslationKey(stack) + ".tooltip";
	}

}
