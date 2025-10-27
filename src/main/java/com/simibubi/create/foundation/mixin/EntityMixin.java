package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;

import net.minecraft.world.entity.Entity;

@Mixin(value = Entity.class, priority = 1500)
public class EntityMixin {
	@ModifyReturnValue(method = "fireImmune()Z", at = @At("RETURN"))
	public boolean create$onFireImmune(boolean original) {
		return ((Entity) (Object) this).getPersistentData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY) || original;
	}
}
