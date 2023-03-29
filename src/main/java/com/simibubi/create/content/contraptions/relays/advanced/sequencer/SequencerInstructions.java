package com.simibubi.create.content.contraptions.relays.advanced.sequencer;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;

public enum SequencerInstructions {

	TURN_ANGLE("angle", AllGuiTextures.SEQUENCER_INSTRUCTION, true, true, 360, 45, 90),
	TURN_DISTANCE("distance", AllGuiTextures.SEQUENCER_INSTRUCTION, true, true, 128, 5, 5),
	DELAY("duration", AllGuiTextures.SEQUENCER_DELAY, true, false, 600, 20, 10),
	AWAIT("", AllGuiTextures.SEQUENCER_AWAIT),
	END("", AllGuiTextures.SEQUENCER_END),

	;

	String translationKey;
	String descriptiveTranslationKey;
	String parameterKey;
	boolean hasValueParameter;
	boolean hasSpeedParameter;
	AllGuiTextures background;
	int maxValue;
	int shiftStep;
	int defaultValue;

	private SequencerInstructions(String parameterName, AllGuiTextures background) {
		this(parameterName, background, false, false, -1, -1, -1);
	}

	private SequencerInstructions(String parameterName, AllGuiTextures background, boolean hasValueParameter,
			boolean hasSpeedParameter, int maxValue, int shiftStep, int defaultValue) {
		this.hasValueParameter = hasValueParameter;
		this.hasSpeedParameter = hasSpeedParameter;
		this.background = background;
		this.maxValue = maxValue;
		this.shiftStep = shiftStep;
		this.defaultValue = defaultValue;
		translationKey = "gui.sequenced_gearshift.instruction." + Lang.asId(name());
		descriptiveTranslationKey = translationKey + ".descriptive";
		parameterKey = translationKey + "." + parameterName;
	}
	
	public boolean needsPropagation() {
		return this == TURN_ANGLE || this == TURN_DISTANCE;
	}

	static List<Component> getOptions() {
		List<Component> options = new ArrayList<>();
		for (SequencerInstructions entry : values())
			options.add(Lang.translateDirect(entry.descriptiveTranslationKey));
		return options;
	}

	String formatValue(int value) {
		if (this == TURN_ANGLE)
			return value + Lang.translateDirect("generic.unit.degrees").getString();
		if (this == TURN_DISTANCE)
			return value + "m";
		if (this == DELAY) {
			if (value >= 20)
				return (value / 20) + "s";
			return value + "t";
		}
		return "" + value;
	}

}
