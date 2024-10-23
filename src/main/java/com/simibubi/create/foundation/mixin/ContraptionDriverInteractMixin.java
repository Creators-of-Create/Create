package com.simibubi.create.foundation.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileUtil.class)
public class ContraptionDriverInteractMixin {
	@WrapOperation(
			method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;canRiderInteract()Z"
			)
	)
	private static boolean create$contraptionDriverCanInteract(Entity instance, Operation<Boolean> original) {
		if (instance.getRootVehicle() instanceof AbstractContraptionEntity)
			return true;
		return original.call(instance);
	}
}
