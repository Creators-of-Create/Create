package com.simibubi.create.foundation.item;

import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import com.mojang.bridge.game.Language;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.flywheel.engine.EngineBlock;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.curiosities.tools.AllToolTiers;
import com.simibubi.create.foundation.item.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TieredItem;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.*;

public class TooltipHelper {

	public static final int maxCharsPerLine = 35;
	public static final Map<String, ItemDescription> cachedTooltips = new HashMap<>();
	public static Language cachedLanguage;
	private static boolean gogglesMode;
	private static final Map<Item, Supplier<String>> tooltipReferrals = new HashMap<>();

	public static IFormattableTextComponent holdShift(Palette color, boolean highlighted) {
		TextFormatting colorFormat = highlighted ? color.hColor : color.color;
		return Lang.translate("tooltip.holdKey", Lang.translate("tooltip.keyShift")
			.formatted(colorFormat)).formatted(TextFormatting.DARK_GRAY);
	}

	public static void addHint(List<ITextComponent> tooltip, String hintKey, Object... messageParams) {
		ITextComponent spacing = IHaveGoggleInformation.componentSpacing;
		tooltip.add(spacing.copy().append(Lang.translate(hintKey + ".title")).formatted(GOLD));
		ITextComponent hint = Lang.translate(hintKey);
		List<ITextComponent> cutComponent = TooltipHelper.cutTextComponent(spacing.copy().append(hint), GRAY, TextFormatting.WHITE);
		for (int i = 0; i < cutComponent.size(); i++)
			tooltip.add((i == 0 ? StringTextComponent.EMPTY : spacing).copy().append(cutComponent.get(i)));
	}
	
	public static void referTo(IItemProvider item, Supplier<? extends IItemProvider> itemWithTooltip) {
		tooltipReferrals.put(item.asItem(), () -> itemWithTooltip.get()
			.asItem()
			.getTranslationKey());
	}

	public static void referTo(IItemProvider item, String string) {
		tooltipReferrals.put(item.asItem(), () -> string);
	}

	@Deprecated
	public static List<String> cutString(ITextComponent s, TextFormatting defaultColor, TextFormatting highlightColor) {
		return cutString(s.getUnformattedComponentText(), defaultColor, highlightColor, 0);
	}

	@Deprecated
	public static List<String> cutString(String s, TextFormatting defaultColor, TextFormatting highlightColor,
		int indent) {
		String lineStart = defaultColor.toString();
		for (int i = 0; i < indent; i++)
			lineStart += " ";

		// Apply markup
		String markedUp = s.replaceAll("_([^_]+)_", highlightColor + "$1" + defaultColor);

		String[] words = markedUp.split(" ");
		List<String> lines = new ArrayList<>();
		StringBuilder currentLine = new StringBuilder(lineStart);
		String word;
		boolean firstWord = true;
		boolean lastWord;

		// Apply hard wrap
		for (int i = 0; i < words.length; i++) {
			word = words[i];
			lastWord = i == words.length - 1;

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

	public static List<ITextComponent> cutStringTextComponent(String c, TextFormatting defaultColor,
		TextFormatting highlightColor) {
		return cutTextComponent(new StringTextComponent(c), defaultColor, highlightColor, 0);
	}

	public static List<ITextComponent> cutTextComponent(ITextComponent c, TextFormatting defaultColor,
		TextFormatting highlightColor) {
		return cutTextComponent(c, defaultColor, highlightColor, 0);
	}

	public static List<ITextComponent> cutStringTextComponent(String c, TextFormatting defaultColor,
		TextFormatting highlightColor, int indent) {
		return cutTextComponent(new StringTextComponent(c), defaultColor, highlightColor, indent);
	}

	public static List<ITextComponent> cutTextComponent(ITextComponent c, TextFormatting defaultColor,
		TextFormatting highlightColor, int indent) {
		IFormattableTextComponent lineStart = StringTextComponent.EMPTY.copy();
		for (int i = 0; i < indent; i++)
			lineStart.append(" ");
		lineStart.formatted(defaultColor);

		List<ITextComponent> lines = new ArrayList<>();
		String rawText = getUnformattedDeepText(c);
		String[] words = rawText.split(" ");
		String word;
		IFormattableTextComponent currentLine = lineStart.copy();

		boolean firstWord = true;
		boolean lastWord;

		// Apply hard wrap
		for (int i = 0; i < words.length; i++) {
			word = words[i];
			lastWord = i == words.length - 1;

			if (!lastWord && !firstWord && getComponentLength(currentLine) + word.length() > maxCharsPerLine) {
				lines.add(currentLine);
				currentLine = lineStart.copy();
				firstWord = true;
			}

			currentLine.append(new StringTextComponent((firstWord ? "" : " ") + word.replace("_", ""))
				.formatted(word.matches("_([^_]+)_") ? highlightColor : defaultColor));
			firstWord = false;
		}

		if (!firstWord) {
			lines.add(currentLine);
		}

		return lines;
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

	public static boolean hasTooltip(ItemStack stack) {
		checkLocale();

		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean hasGlasses =
			player != null && AllItems.GOGGLES.isIn(player.getItemStackFromSlot(EquipmentSlotType.HEAD));

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
			tooltip = tooltip.withSummary(new StringTextComponent(I18n.format(summaryKey)));

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
		Item item = stack.getItem();
		if (item instanceof TieredItem) {
			TieredItem tieredItem = (TieredItem) item;
			if (tieredItem.getTier() instanceof AllToolTiers) {
				AllToolTiers allToolTiers = (AllToolTiers) tieredItem.getTier();
				return "tool.create." + Lang.asId(allToolTiers.name()) + ".tooltip";
			}
		}

		if (tooltipReferrals.containsKey(item))
			return tooltipReferrals.get(item).get() + ".tooltip";
		return item.getTranslationKey(stack) + ".tooltip";
	}

	private static int getComponentLength(ITextComponent component) {
		AtomicInteger l = new AtomicInteger();
		TextProcessing.visitFormatted(component, Style.EMPTY, (s, style, charConsumer) -> {
			l.getAndIncrement();
			return true;
		});
		return l.get();
	}

	public static String getUnformattedDeepText(ITextComponent component) {
		StringBuilder b = new StringBuilder();
		if (!(component instanceof TranslationTextComponent));
			b.append(component.getString());
		component.getSiblings()
			.forEach(c -> b.append(getUnformattedDeepText(c)));
		return b.toString();
	}

}
