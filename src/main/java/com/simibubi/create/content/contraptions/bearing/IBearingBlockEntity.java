package com.simibubi.create.content.contraptions.bearing;

import com.simibubi.create.content.contraptions.DirectionalExtenderScrollOptionSlot;
import com.simibubi.create.content.contraptions.IControlContraption;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import net.minecraft.core.Direction.Axis;

public interface IBearingBlockEntity extends IControlContraption {

	float getInterpolatedAngle(float partialTicks);

	boolean isWoodenTop();

	default ValueBoxTransform getMovementModeSlot() {
		return new DirectionalExtenderScrollOptionSlot((state, d) -> {
			Axis axis = d.getAxis();
			Axis bearingAxis = state.getValue(BearingBlock.FACING)
				.getAxis();
			return bearingAxis != axis;
		});
	}
	
	void setAngle(float forcedAngle);

}
