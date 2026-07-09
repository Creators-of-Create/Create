package com.simibubi.create.content.processing.burner;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import net.createmod.catnip.api.client.render.SpriteShiftEntry;

public class ScrollTransformedInstance extends TransformedInstance {
	public float speedU;
	public float speedV;

	public float offsetU;
	public float offsetV;

	public float diffU;
	public float diffV;

	public float scaleU;
	public float scaleV;

	public ScrollTransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public ScrollTransformedInstance setSpriteShift(SpriteShiftEntry spriteShift) {
		return setSpriteShift(spriteShift, 0.5f, 0.5f);
	}

	public ScrollTransformedInstance setSpriteShift(SpriteShiftEntry spriteShift, float factorU, float factorV) {
		float spriteWidth = spriteShift.getTarget()
			.getU1()
			- spriteShift.getTarget()
			.getU0();

		float spriteHeight = spriteShift.getTarget()
			.getV1()
			- spriteShift.getTarget()
			.getV0();

		scaleU = spriteWidth * factorU;
		scaleV = spriteHeight * factorV;

		diffU = spriteShift.getTarget().getU0() - spriteShift.getOriginal().getU0();
		diffV = spriteShift.getTarget().getV0() - spriteShift.getOriginal().getV0();

		return this;
	}

	public ScrollTransformedInstance speed(float speedU, float speedV) {
		this.speedU = speedU;
		this.speedV = speedV;
		return this;
	}

	public ScrollTransformedInstance offset(float offsetU, float offsetV) {
		this.offsetU = offsetU;
		this.offsetV = offsetV;
		return this;
	}
}
