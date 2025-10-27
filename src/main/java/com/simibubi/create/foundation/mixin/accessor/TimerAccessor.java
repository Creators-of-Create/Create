package com.simibubi.create.foundation.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.DeltaTracker;

@Mixin(DeltaTracker.Timer.class)
public interface TimerAccessor {
	@Accessor
	float getDeltaTickResidual();
}
