package com.simibubi.create.content.trains.schedule;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

public class DestinationSuggestions extends CommandSuggestions {

	private EditBox textBox;
	private List<IntAttached<String>> viableStations;
	private String previous = "<>";
	private Font font;
	private boolean active;

	List<Suggestion> currentSuggestions;
	private int yOffset;

	public DestinationSuggestions(Minecraft pMinecraft, Screen pScreen, EditBox pInput, Font pFont,
		List<IntAttached<String>> viableStations, int yOffset) {
		super(pMinecraft, pScreen, pInput, pFont, true, true, 0, 7, false, 0xee_303030);
		this.textBox = pInput;
		this.font = pFont;
		this.viableStations = viableStations;
		this.yOffset = yOffset;
		currentSuggestions = new ArrayList<>();
		active = false;
	}

	public void tick() {
		if (suggestions == null)
			textBox.setSuggestion("");
		if (active == textBox.isFocused())
			return;
		active = textBox.isFocused();
		updateCommandInfo();
	}

	@Override
	public void updateCommandInfo() {
		String value = this.textBox.getValue();
		if (value.equals(previous))
			return;
		if (!active) {
			suggestions = null;
			return;
		}

		previous = value;
		currentSuggestions = viableStations.stream()
			.filter(ia -> !ia.getValue()
				.equals(value) && ia.getValue()
					.toLowerCase()
					.startsWith(value.toLowerCase()))
			.sorted((ia1, ia2) -> Integer.compare(ia1.getFirst(), ia2.getFirst()))
			.map(IntAttached::getValue)
			.map(s -> new Suggestion(new StringRange(0, s.length()), s))
			.toList();

		showSuggestions(false);
	}

	public void showSuggestions(boolean pNarrateFirstSuggestion) {
		if (currentSuggestions.isEmpty()) {
			suggestions = null;
			return;
		}

		int width = 0;
		for (Suggestion suggestion : currentSuggestions)
			width = Math.max(width, this.font.width(suggestion.getText()));
		int x = Mth.clamp(textBox.getScreenX(0), 0, textBox.getScreenX(0) + textBox.getInnerWidth() - width);
		suggestions = new CommandSuggestions.SuggestionsList(x, 72 + yOffset, width, currentSuggestions, false);
	}

}
