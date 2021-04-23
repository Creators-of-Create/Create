package com.simibubi.create.content.curiosities.projector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.render.backend.effects.ColorMatrices;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

public class ColorEffect {
	static final ArrayList<ColorEffect> all = new ArrayList<>();
	static final HashMap<String, ColorEffect> lookup = new HashMap<>();
	private static int nextId = 0;

	public static final ColorEffect INVERT = create("invert", ColorMatrices::invert);
	public static final ColorEffect SEPIA = create("sepia", ColorMatrices::sepia);
	public static final ColorEffect GRAYSCALE = create("grayscale", ColorMatrices::grayscale);
	public static final ColorEffect DARKEN = create("darken", ColorMatrices::darken).setDefaultValue(20);
	public static final ColorEffect SATURATE = create("saturate", ColorMatrices::saturate).setRange(0, 200);
	public static final ColorEffect HUE_SHIFT = create("hue_shift", ColorMatrices::hueShift).setRange(0, 360).setDivisor(1f).setDefaultValue(120);
	public static final ColorEffect END = create("end", ColorMatrices::identity).setBackground(AllGuiTextures.PROJECTOR_END);

	boolean hasParameter;
	AllGuiTextures background;

	int defaultValue = 100;
	int minValue = 0;
	int maxValue = 100;
	float divisor = 100f;

	final int id;
	final FilterFactory filter;
	final String name;
	final String translationKey;

	public ColorEffect(String name, FilterFactory filter) {
		this.filter = filter;
		this.name = name;
		this.translationKey = "gui.chromatic_projector.filter." + Lang.asId(name);
		this.id = nextId++;

		lookup.put(name, this);
		all.add(this);
	}

	public ColorEffect setHasParameter(boolean hasParameter) {
		this.hasParameter = hasParameter;
		return setBackground(hasParameter ? AllGuiTextures.PROJECTOR_FILTER_STRENGTH : AllGuiTextures.PROJECTOR_FILTER);
	}

	public ColorEffect setBackground(AllGuiTextures background) {
		this.background = background;
		return this;
	}

	public ColorEffect setDefaultValue(int defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public ColorEffect setRange(int minValue, int maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		return this;
	}

	public ColorEffect setDivisor(float divisor) {
		this.divisor = divisor;
		return this;
	}

	public Function<ScrollValueBehaviour.StepContext, Integer> step() {
		return c -> {
			if (c.control) return 1;
			if (c.shift) return 20;
			return 5;
		};
	}

	String formatValue(int value) {
		if (this == HUE_SHIFT)
			return value + Lang.translate("generic.unit.degrees").getString();
		return "" + value;
	}

	static List<ITextComponent> getOptions() {
		List<ITextComponent> options = new ArrayList<>();
		for (ColorEffect entry : all)
			options.add(Lang.translate(entry.translationKey));
		return options;
	}

	@FunctionalInterface
	public interface FilterFactory {
		Matrix4f create(float param);
	}

	public static ColorEffect create(String name, Supplier<Matrix4f> filter) {
		return new ColorEffect(name, $ -> filter.get()).setHasParameter(false);
	}

	public static ColorEffect create(String name, FilterFactory filter) {
		return new ColorEffect(name, filter).setHasParameter(true);
	}
}
