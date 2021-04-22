package com.simibubi.create.content.curiosities.projector;

import java.util.Vector;

public class FilterStep {

	ColorEffects instruction;
	int value;

	public FilterStep(ColorEffects instruction) {
		this.instruction = instruction;
	}

	public FilterStep(ColorEffects instruction, int value) {
		this.instruction = instruction;
		this.value = value;
	}

	public static Vector<FilterStep> createDefault() {
		Vector<FilterStep> instructions = new Vector<>(ChromaticProjectorScreen.MAX_STEPS);
		instructions.add(new FilterStep(ColorEffects.SEPIA, 100));
		instructions.add(new FilterStep(ColorEffects.END));
		return instructions;
	}
}
