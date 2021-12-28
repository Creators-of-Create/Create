package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.entity.RemovalFromWorldListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.client.multiplayer.ClientLevel.EntityCallbacks")
public abstract class ClientLevel$EntityCallbacksMixin {
	@Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("RETURN"))
	private void create$onTrackingEnd(Entity entity, CallbackInfo ci) {
		if (entity instanceof RemovalFromWorldListener listener) {
			listener.onRemovedFromWorld();
		}
	}
}
