package com.simibubi.create.content.curiosities.projector;

import java.util.Iterator;
import java.util.Vector;

import com.simibubi.create.foundation.render.backend.effects.ColorMatrices;

import net.minecraft.util.math.vector.Matrix4f;

public class FilterStep {

	ColorEffects filter;
	int value;

	public FilterStep(ColorEffects filter) {
		this.filter = filter;
	}

	public FilterStep(ColorEffects filter, int value) {
		this.filter = filter;
		this.value = value;
	}

	public Matrix4f createFilter() {
		return filter.filter.create(value / filter.divisor);
	}

	public static Matrix4f fold(Vector<FilterStep> filters) {
		Iterator<FilterStep> stepIterator = filters.stream().filter(it -> it != null && it.filter != ColorEffects.END).iterator();

		if (stepIterator.hasNext()) {
			Matrix4f accum = stepIterator.next().createFilter();

			stepIterator.forEachRemaining(filterStep -> accum.multiply(filterStep.createFilter()));

			return accum;
		}

		return ColorMatrices.identity();
	}

	public static Vector<FilterStep> createDefault() {
		Vector<FilterStep> instructions = new Vector<>(ChromaticProjectorScreen.MAX_STEPS);
		instructions.add(new FilterStep(ColorEffects.SEPIA, 100));
		instructions.add(new FilterStep(ColorEffects.END));
		return instructions;
	}
}
