package com.simibubi.create.content.kinetics.belt;

import net.createmod.catnip.lang.Lang;
import net.minecraft.util.StringRepresentable;

public enum BeltSlope implements StringRepresentable {
	HORIZONTAL, UPWARD, DOWNWARD, VERTICAL, SIDEWAYS, DIAGONAL_SIDEWAYS;

	@Override
	public String getSerializedName() {
		return Lang.asId(name());
	}

	public boolean isDiagonal() {
		return this == UPWARD || this == DOWNWARD || this == DIAGONAL_SIDEWAYS;
	}

	public boolean isSideways() {
		return this == SIDEWAYS || this == DIAGONAL_SIDEWAYS;
	}
}
