package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;

@Mixin(value = Entity.class, priority = 1500)
public class EntityMixin {
	@ModifyExpressionValue(method = "canEnterPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
	public boolean create$playerHidingAsBoxIsCrouchingNotSwimming(boolean original, @Local(argsOnly = true) Pose pose) {
		return original || (pose == Pose.CROUCHING && CardboardArmorHandler.testForStealth((Entity) (Object) this));
	}

	@ModifyReturnValue(method = "fireImmune()Z", at = @At("RETURN"))
	public boolean create$onFireImmune(boolean original) {
		return ((Entity) (Object) this).getPersistentData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY) || original;
	}
}
