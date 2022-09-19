package com.simibubi.create.foundation.block.connected;

import net.createmod.catnip.render.SpriteShiftEntry;
import net.minecraft.resources.ResourceLocation;

public class CTSpriteShiftEntry extends SpriteShiftEntry {

	protected final CTType type;

	public CTSpriteShiftEntry(CTType type, ResourceLocation originalTextureLocation, ResourceLocation targetTextureLocation) {
		super(originalTextureLocation, targetTextureLocation);
		this.type = type;
	}

	public CTType getType() {
		return type;
	}

	public float getTargetU(float localU, int index) {
		float uOffset = (index % type.getSheetSize());
		return getTarget().getU(
			(getUnInterpolatedU(getOriginal(), localU) + (uOffset * 16)) / ((float) type.getSheetSize()));
	}

	public float getTargetV(float localV, int index) {
		float vOffset = (index / type.getSheetSize());
		return getTarget().getV(
			(getUnInterpolatedV(getOriginal(), localV) + (vOffset * 16)) / ((float) type.getSheetSize()));
	}

}
