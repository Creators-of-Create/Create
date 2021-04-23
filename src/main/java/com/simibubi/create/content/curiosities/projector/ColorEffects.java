package com.simibubi.create.content.curiosities.projector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.render.backend.effects.ColorMatrices;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

public enum ColorEffects {
	INVERT(ColorMatrices::invert),
	SEPIA(ColorMatrices::sepia),
	GRAYSCALE(ColorMatrices::grayscale),
	DARKEN(ColorMatrices::darken),
	SATURATE(ColorMatrices::saturate, 0, 200),
	HUE_SHIFT(ColorMatrices::hueShift, 0, 360, 1f),
	END(ColorMatrices::identity, AllGuiTextures.PROJECTOR_END),

	;

	FilterFactory filter;
	boolean hasParameter;
	String translationKey;
	AllGuiTextures background;

	int minValue = 0;
	int maxValue = 100;
	float divisor = 100f;

	ColorEffects(Supplier<Matrix4f> filter, AllGuiTextures background) {
		this($ -> filter.get(), false, background);
	}

	ColorEffects(Supplier<Matrix4f> filter) {
		this($ -> filter.get(), false, AllGuiTextures.PROJECTOR_FILTER);
	}

	ColorEffects(FilterFactory filter) {
		this(filter, 0, 100);
	}

	ColorEffects(FilterFactory filter, int minValue, int maxValue) {
		this(filter, minValue, maxValue, 100f);
	}

	ColorEffects(FilterFactory filter, int minValue, int maxValue, float divisor) {
		this(filter, true, AllGuiTextures.PROJECTOR_FILTER_STRENGTH);
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.divisor = divisor;
	}

	ColorEffects(FilterFactory filter, boolean hasParameter, AllGuiTextures background) {
		this.filter = filter;
		this.hasParameter = hasParameter;
		this.background = background;
		translationKey = "gui.chromatic_projector.filter." + Lang.asId(name());
	}

	String formatValue(int value) {
		if (this == HUE_SHIFT)
			return value + Lang.translate("generic.unit.degrees").getString();
		return "" + value;
	}

	static List<ITextComponent> getOptions() {
		List<ITextComponent> options = new ArrayList<>();
		for (ColorEffects entry : values())
			options.add(Lang.translate(entry.translationKey));
		return options;
	}

	@FunctionalInterface
	public interface FilterFactory {
		Matrix4f create(float param);
	}
}
