package com.simibubi.create.foundation.item;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.WHITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.bridge.game.Language;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.utility.FontHelper;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public record ItemDescription(ImmutableList<Component> lines, ImmutableList<Component> linesOnShift, ImmutableList<Component> linesOnCtrl) {
	private static final Map<Item, Supplier<String>> CUSTOM_TOOLTIP_KEYS = new IdentityHashMap<>();

	@Nullable
	public static ItemDescription create(Item item, FontHelper.Palette palette) {
		return create(getTooltipTranslationKey(item), palette);
	}

	@Nullable
	public static ItemDescription create(String translationKey, FontHelper.Palette palette) {
		if (!canFillBuilder(translationKey)) {
			return null;
		}

		Builder builder = new Builder(palette);
		fillBuilder(builder, translationKey);
		return builder.build();
	}

	public static boolean canFillBuilder(String translationKey) {
		return I18n.exists(translationKey);
	}

	public static void fillBuilder(Builder builder, String translationKey) {
		// Summary
		String summaryKey = translationKey + ".summary";
		if (I18n.exists(summaryKey)) {
			builder.addSummary(I18n.get(summaryKey));
		}

		// Behaviours
		for (int i = 1; i < 100; i++) {
			String conditionKey = translationKey + ".condition" + i;
			String behaviourKey = translationKey + ".behaviour" + i;
			if (!I18n.exists(conditionKey))
				break;
			builder.addBehaviour(I18n.get(conditionKey), I18n.get(behaviourKey));
		}

		// Actions
		for (int i = 1; i < 100; i++) {
			String controlKey = translationKey + ".control" + i;
			String actionKey = translationKey + ".action" + i;
			if (!I18n.exists(controlKey))
				break;
			builder.addAction(I18n.get(controlKey), I18n.get(actionKey));
		}
	}

	public static void useKey(Item item, Supplier<String> supplier) {
		CUSTOM_TOOLTIP_KEYS.put(item, supplier);
	}

	public static void useKey(ItemLike item, String string) {
		useKey(item.asItem(), () -> string);
	}

	public static void referKey(ItemLike item, Supplier<? extends ItemLike> otherItem) {
		useKey(item.asItem(), () -> otherItem.get()
			.asItem()
			.getDescriptionId());
	}

	public static String getTooltipTranslationKey(Item item) {
		if (CUSTOM_TOOLTIP_KEYS.containsKey(item)) {
			return CUSTOM_TOOLTIP_KEYS.get(item).get() + ".tooltip";
		}
		return item.getDescriptionId() + ".tooltip";
	}

	public ImmutableList<Component> getCurrentLines() {
		if (Screen.hasShiftDown()) {
			return linesOnShift;
		} else if (Screen.hasControlDown()) {
			return linesOnCtrl;
		} else {
			return lines;
		}
	}

	public static class Builder {
		protected final FontHelper.Palette palette;
		protected final List<String> summary = new ArrayList<>();
		protected final List<Pair<String, String>> behaviours = new ArrayList<>();
		protected final List<Pair<String, String>> actions = new ArrayList<>();

		public Builder(FontHelper.Palette palette) {
			this.palette = palette;
		}

		public Builder addSummary(String summaryLine) {
			summary.add(summaryLine);
			return this;
		}

		public Builder addBehaviour(String condition, String behaviour) {
			behaviours.add(Pair.of(condition, behaviour));
			return this;
		}

		public Builder addAction(String condition, String action) {
			actions.add(Pair.of(condition, action));
			return this;
		}

		public ItemDescription build() {
			List<Component> lines = new ArrayList<>();
			List<Component> linesOnShift = new ArrayList<>();
			List<Component> linesOnCtrl = new ArrayList<>();

			for (String summaryLine : summary) {
				linesOnShift.addAll(TooltipHelper.cutStringTextComponent(summaryLine, palette));
			}

			if (!behaviours.isEmpty()) {
				linesOnShift.add(Components.immutableEmpty());
			}

			for (Pair<String, String> behaviourPair : behaviours) {
				String condition = behaviourPair.getLeft();
				String behaviour = behaviourPair.getRight();
				linesOnShift.add(Components.literal(condition).withStyle(GRAY));
				linesOnShift.addAll(TooltipHelper.cutStringTextComponent(behaviour, palette.primary(), palette.highlight(), 1));
			}

			for (Pair<String, String> actionPair : actions) {
				String condition = actionPair.getLeft();
				String action = actionPair.getRight();
				linesOnCtrl.add(Components.literal(condition).withStyle(GRAY));
				linesOnCtrl.addAll(TooltipHelper.cutStringTextComponent(action, palette.primary(), palette.highlight(), 1));
			}

			boolean hasDescription = !linesOnShift.isEmpty();
			boolean hasControls = !linesOnCtrl.isEmpty();

			if (hasDescription || hasControls) {
				String[] holdDesc = CreateLang.translateDirect("tooltip.holdForDescription", "$")
					.getString()
					.split("\\$");
				String[] holdCtrl = CreateLang.translateDirect("tooltip.holdForControls", "$")
					.getString()
					.split("\\$");
				MutableComponent keyShift = CreateLang.translateDirect("tooltip.keyShift");
				MutableComponent keyCtrl = CreateLang.translateDirect("tooltip.keyCtrl");
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

			if (!hasDescription) {
				linesOnCtrl.clear();
				linesOnShift.addAll(lines);
			}
			if (!hasControls) {
				linesOnCtrl.clear();
				linesOnCtrl.addAll(lines);
			}

			return new ItemDescription(ImmutableList.copyOf(lines), ImmutableList.copyOf(linesOnShift), ImmutableList.copyOf(linesOnCtrl));
		}
	}

	public static class Modifier implements TooltipModifier {
		protected final Item item;
		protected final FontHelper.Palette palette;
		protected Language cachedLanguage;
		protected ItemDescription description;

		public Modifier(Item item, FontHelper.Palette palette) {
			this.item = item;
			this.palette = palette;
		}

		@Override
		public void modify(ItemTooltipEvent context) {
			if (checkLocale()) {
				description = create(item, palette);
			}
			if (description == null) {
				return;
			}
			context.getToolTip().addAll(1, description.getCurrentLines());
		}

		protected boolean checkLocale() {
			Language currentLanguage = Minecraft.getInstance()
				.getLanguageManager()
				.getSelected();
			if (cachedLanguage != currentLanguage) {
				cachedLanguage = currentLanguage;
				return true;
			}
			return false;
		}
	}
}
