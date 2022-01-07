package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.event.ProjectileImpactCallback;

import net.minecraft.world.entity.projectile.Projectile;

import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.phys.HitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Projectile.class, ThrownEgg.class})
public abstract class ProjectileMixin {
	@Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
	private void create$onProjectileHit(HitResult result, CallbackInfo ci) {
		if (ProjectileImpactCallback.EVENT.invoker().onImpact((Projectile) (Object) this, result)) {
			ci.cancel();
		}
	}
}
