package com.simibubi.create.lib.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.SERVER)
@Mixin(Vec3.class)
public abstract class Vec3Mixin {
	@Shadow
	public abstract Vec3 scale(double mult);

	// They are client-only, but not anymore!

	public Vec3 inverse() {
		return scale(-1.0D);
	}

	public Vec3 method_22882() {
		return inverse();
	}
}
