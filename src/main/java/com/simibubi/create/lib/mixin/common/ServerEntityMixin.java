package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.events.CommonEvents;

import com.simibubi.create.lib.entity.ExtraSpawnDataEntity;

import com.simibubi.create.lib.util.EntityHelper;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;

import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Inject(method = "addPairing", at = @At("TAIL"))
	private void create$addPairing(ServerPlayer player, CallbackInfo ci) {
		CommonEvents.startTracking(entity, player);
	}

	@Inject(
			method = "sendPairingData",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
					ordinal = 0,
					shift = Shift.AFTER
			)
	)
	private void create$entityPacketSend(Consumer<Packet<?>> packetConsumer, CallbackInfo ci) {
		if (entity instanceof ExtraSpawnDataEntity) {
			EntityHelper.handleDataSend(entity, packetConsumer);
		}
	}
}
