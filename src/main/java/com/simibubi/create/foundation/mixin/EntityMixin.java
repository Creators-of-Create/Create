package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;

import net.minecraft.world.entity.Entity;

@Mixin(value = Entity.class, priority = 900)
public class EntityMixin {
	@Inject(method = "fireImmune()Z", at = @At("RETURN"), cancellable = true)
	private void create$onFireImmune(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			if (((Entity) (Object) this).getPersistentData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY))
				cir.setReturnValue(true);
		}
	}
}
