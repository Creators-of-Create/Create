package com.simibubi.create.lib.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.event.OnDatapackSyncCallback;
import com.simibubi.create.lib.util.MixinHelper;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 4, shift = At.Shift.AFTER))
	public void onDataSync(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {
		OnDatapackSyncCallback.EVENT.invoker().onDatapackSync(MixinHelper.<PlayerList>cast(this), serverPlayer);
	}

	@Inject(method = "reloadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V", shift = At.Shift.BEFORE))
	public void onReloadResource(CallbackInfo ci) {
		OnDatapackSyncCallback.EVENT.invoker().onDatapackSync(MixinHelper.<PlayerList>cast(this), null);
	}
}
