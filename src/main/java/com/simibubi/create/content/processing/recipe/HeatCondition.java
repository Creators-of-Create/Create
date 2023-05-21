package com.simibubi.create.content.processing.recipe;

import com.simibubi.create.Create;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.utility.Lang;

public enum HeatCondition {

	NONE(0xffffff), HEATED(0xE88300), SUPERHEATED(0x5C93E8),

	;

	private int color;

	private HeatCondition(int color) {
		this.color = color;
	}

	public boolean testBlazeBurner(BlazeBurnerBlock.HeatLevel level) {
		if (this == SUPERHEATED)
			return level == HeatLevel.SEETHING;
		if (this == HEATED)
			return level != HeatLevel.NONE && level != HeatLevel.SMOULDERING;
		return true;
	}

	public BlazeBurnerBlock.HeatLevel visualizeAsBlazeBurner() {
		if (this == SUPERHEATED)
			return HeatLevel.SEETHING;
		if (this == HEATED)
			return HeatLevel.KINDLED;
		return HeatLevel.NONE;
	}

	public String serialize() {
		return Lang.asId(name());
	}

	public String getTranslationKey() {
		return "recipe.heat_requirement." + serialize();
	}

	public static HeatCondition deserialize(String name) {
		for (HeatCondition heatCondition : values())
			if (heatCondition.serialize()
					.equals(name))
				return heatCondition;
		Create.LOGGER.warn("Tried to deserialize invalid heat condition: \"" + name + "\"");
		return NONE;
	}

	public int getColor() {
		return color;
	}

}
