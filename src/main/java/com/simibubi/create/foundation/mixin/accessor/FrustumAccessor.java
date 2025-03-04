package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.client.renderer.culling.Frustum;

import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Frustum.class)
public interface FrustumAccessor {
	@Accessor("intersection")
	FrustumIntersection create$getFrustumIntersection();
}
