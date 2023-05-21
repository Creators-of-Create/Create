package com.simibubi.create.content.kinetics.belt;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.util.StringRepresentable;

public enum BeltPart implements StringRepresentable {
	START, MIDDLE, END, PULLEY;

	@Override
	public String getSerializedName() {
		return Lang.asId(name());
	}
}