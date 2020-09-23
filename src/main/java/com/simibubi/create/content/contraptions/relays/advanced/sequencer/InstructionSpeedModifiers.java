package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.util.text.ITextComponent;

public enum InstructionSpeedModifiers {

	FORWARD_FAST(2, ">>"), FORWARD(1, "->"), BACK(-1, "<-"), BACK_FAST(-2, "<<"),

	;

	String translationKey;
	int value;
	ITextComponent label;

	private InstructionSpeedModifiers(int modifier, ITextComponent label) {
		this.label = label;
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}
	private InstructionSpeedModifiers(int modifier, String label) {
		this.label = ITextComponent.of(label);
		translationKey = "gui.sequenced_gearshift.speed." + Lang.asId(name());
		value = modifier;
	}

	static List<ITextComponent> getOptions() {
		List<ITextComponent> options = new ArrayList<>();
		for (InstructionSpeedModifiers entry : values())
			options.add(Lang.translate(entry.translationKey));
		return options;
	}

}
