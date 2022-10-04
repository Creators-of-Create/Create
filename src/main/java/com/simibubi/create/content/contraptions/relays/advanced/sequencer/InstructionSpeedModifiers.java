package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;

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
		this.label = Components.literal(label);
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}

	static List<Component> getOptions() {
		List<Component> options = new ArrayList<>();
		for (InstructionSpeedModifiers entry : values())
			options.add(Lang.translateDirect(entry.translationKey));
		return options;
	}

	public static InstructionSpeedModifiers getByModifier(int modifier) {
		return Arrays.stream(InstructionSpeedModifiers.values())
				.filter(speedModifier -> speedModifier.value == modifier)
				.findAny()
				.orElse(InstructionSpeedModifiers.FORWARD);
	}

}
