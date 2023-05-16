package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.curiosities.armor.NetheriteDivingHandler;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "fireImmune()Z", at = @At("RETURN"), cancellable = true)
	public void create$onFireImmune(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()) {
			Entity self = (Entity) (Object) this;
			boolean immune = self.getPersistentData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY);
			cir.setReturnValue(immune);
		}
	}
}
