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
	SATURATE(ColorMatrices::saturate),
	HUE_SHIFT(ColorMatrices::hueShift),
	END(ColorMatrices::identity, AllGuiTextures.PROJECTOR_END),

	;

	FilterFactory filter;
	boolean hasParameter;
	String translationKey;
	AllGuiTextures background;

	ColorEffects(Supplier<Matrix4f> filter, AllGuiTextures background) {
		this($ -> filter.get(), false, background);
	}

	ColorEffects(Supplier<Matrix4f> filter) {
		this($ -> filter.get(), false, AllGuiTextures.PROJECTOR_FILTER);
	}

	ColorEffects(FilterFactory filter) {
		this(filter, true, AllGuiTextures.PROJECTOR_FILTER_STRENGTH);
	}

	ColorEffects(FilterFactory filter, boolean hasParameter, AllGuiTextures background) {
		this.filter = filter;
		this.hasParameter = hasParameter;
		this.background = background;
		translationKey = "gui.chromatic_projector.filter." + Lang.asId(name());
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
