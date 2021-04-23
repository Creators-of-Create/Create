package com.simibubi.create.content.curiosities.projector;

import java.util.Iterator;
import java.util.Vector;

import com.simibubi.create.foundation.render.backend.effects.ColorMatrices;

import net.minecraft.util.math.vector.Matrix4f;

public class FilterStep {

	ColorEffect filter;
	int value;

	public FilterStep(ColorEffect filter) {
		this.filter = filter;
	}

	public FilterStep(ColorEffect filter, int value) {
		this.filter = filter;
		this.value = value;
	}

	public Matrix4f createFilter() {
		return filter.filter.create(value / filter.divisor);
	}

	public static Matrix4f fold(Vector<FilterStep> filters) {
		Iterator<FilterStep> stepIterator = filters.stream().filter(it -> it != null && it.filter != ColorEffect.END).iterator();

		if (stepIterator.hasNext()) {
			Matrix4f accum = stepIterator.next().createFilter();

			stepIterator.forEachRemaining(filterStep -> accum.multiply(filterStep.createFilter()));

			return accum;
		}

		return ColorMatrices.identity();
	}

	public static Vector<FilterStep> createDefault() {
		Vector<FilterStep> instructions = new Vector<>(ChromaticProjectorScreen.MAX_STEPS);
		instructions.add(new FilterStep(ColorEffect.SEPIA, 100));
		instructions.add(new FilterStep(ColorEffect.END));
		return instructions;
	}
}
