package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public enum InstructionSpeedModifiers {

	FORWARD_FAST(2, ">>"), FORWARD(1, "->"), BACK(-1, "<-"), BACK_FAST(-2, "<<"),

	;

	String translationKey;
	int value;
	Component label;

	private InstructionSpeedModifiers(int modifier, Component label) {
		this.label = label;
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}
	private InstructionSpeedModifiers(int modifier, String label) {
		this.label = new TextComponent(label);
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}

	static List<Component> getOptions() {
		List<Component> options = new ArrayList<>();
		for (InstructionSpeedModifiers entry : values())
			options.add(Lang.translate(entry.translationKey));
		return options;
	}

}
