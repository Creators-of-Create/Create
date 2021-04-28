package com.simibubi.create.content.curiosities.projector;

import java.util.Iterator;
import java.util.Vector;

import com.simibubi.create.foundation.render.backend.effects.ColorMatrices;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.vector.Matrix4f;

public class FilterStep {

	public static final int MAX_STEPS = 6;
	ColorEffect filter;
	int value;

	public FilterStep(ColorEffect filter) {
		this.filter = filter;
	}

	public FilterStep(ColorEffect filter, int value) {
		this.filter = filter;
		this.value = value;
	}

	public FilterStep(CompoundNBT nbt) {
		this.filter = ColorEffect.lookup.get(nbt.getString("id"));
		this.value = nbt.getInt("value");
	}

	public Matrix4f createFilter() {
		return filter.filter.create(value / filter.divisor);
	}

	public CompoundNBT write() {
		CompoundNBT nbt = new CompoundNBT();

		nbt.putString("id", filter.name);
		nbt.putInt("value", value);

		return nbt;
	}

	public static Vector<FilterStep> readAll(ListNBT list) {
		Vector<FilterStep> steps = new Vector<>(MAX_STEPS);

		for (int i = 0; i < list.size(); i++) {
			steps.add(new FilterStep(list.getCompound(i)));
		}

		return steps;
	}

	public static ListNBT writeAll(Vector<FilterStep> filters) {
		ListNBT out = new ListNBT();

		for (FilterStep filter : filters) {
			out.add(filter.write());
		}

		return out;
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
		Vector<FilterStep> instructions = new Vector<>(MAX_STEPS);
		instructions.add(new FilterStep(ColorEffect.SEPIA, 100));
		instructions.add(new FilterStep(ColorEffect.END));
		return instructions;
	}
}
