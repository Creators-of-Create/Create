package com.simibubi.create.lib.mixin.client;

import com.simibubi.create.lib.entity.RemovalFromWorldListener;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "net.minecraft.client.multiplayer.ClientLevel.EntityCallbacks")
public class ClientLevel$EntityCallbacksMixin {
	@Inject(at = @At("RETURN"), method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V")
	private void create$onTrackingEnd(Entity entity, CallbackInfo ci) {
		if (entity instanceof RemovalFromWorldListener listener) {
			listener.onRemovedFromWorld();
		}
	}
}
