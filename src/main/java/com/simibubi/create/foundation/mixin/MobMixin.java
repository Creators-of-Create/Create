package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

@Mixin(Mob.class)
public class MobMixin {
	@ModifyExpressionValue(
		method = "getAttackBoundingBox",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/Mob;getVehicle()Lnet/minecraft/world/entity/Entity;"
			)
		)
	public Entity create$mobRidingContraptionsMaintainTheirAttackBox(Entity original) {
		return original instanceof AbstractContraptionEntity ? null : original;
	}
}
