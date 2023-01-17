package com.simibubi.create.foundation.item;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.WHITE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record ItemDescription(ImmutableList<Component> lines, ImmutableList<Component> linesOnShift, ImmutableList<Component> linesOnCtrl) {
	public static final ItemDescription MISSING = new ItemDescription(ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

	public static Builder builder() {
		return new Builder();
	}

	public static ItemDescription create(Palette palette, String translationKey) {
		if (!I18n.exists(translationKey)) {
			return MISSING;
		}

		Builder builder = builder();
		builder.palette(palette);

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

		return builder.build();
	}

	public void addInformation(List<Component> tooltip) {
		if (Screen.hasShiftDown()) {
			tooltip.addAll(linesOnShift);
			return;
		}

		if (Screen.hasControlDown()) {
			tooltip.addAll(linesOnCtrl);
			return;
		}

		tooltip.addAll(lines);
	}

	public record Palette(ChatFormatting primary, ChatFormatting highlight) {
		public static final Palette BLUE = new Palette(ChatFormatting.BLUE, ChatFormatting.AQUA);
		public static final Palette GREEN = new Palette(ChatFormatting.DARK_GREEN, ChatFormatting.GREEN);
		public static final Palette YELLOW = new Palette(ChatFormatting.GOLD, ChatFormatting.YELLOW);
		public static final Palette RED = new Palette(ChatFormatting.DARK_RED, ChatFormatting.RED);
		public static final Palette PURPLE = new Palette(ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
		public static final Palette GRAY = new Palette(ChatFormatting.DARK_GRAY, ChatFormatting.GRAY);
	}

	public static class Builder {
		protected final List<String> summary = new ArrayList<>();
		protected final List<Pair<String, String>> behaviours = new ArrayList<>();
		protected final List<Pair<String, String>> actions = new ArrayList<>();
		protected Palette palette;

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

		public Builder palette(Palette palette) {
			this.palette = palette;
			return this;
		}

		public ItemDescription build() {
			List<Component> lines = new ArrayList<>();
			List<Component> linesOnShift = new ArrayList<>();
			List<Component> linesOnCtrl = new ArrayList<>();

			for (String summaryLine : summary) {
				linesOnShift.addAll(TooltipHelper.cutTextComponent(Components.literal(summaryLine), palette.primary(), palette.highlight()));
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
}
