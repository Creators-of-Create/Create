package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.lib.block.CustomDataPacketHandlingBlockEntity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
	@Shadow
	@Final
	private Connection connection;

	@Inject(method = "method_38542", at = @At("HEAD"), cancellable = true)
	public void create$handleCustomBlockEntity(ClientboundBlockEntityDataPacket packet, BlockEntity blockEntity, CallbackInfo ci) {
		if (blockEntity instanceof CustomDataPacketHandlingBlockEntity handler) {
			handler.onDataPacket(connection, packet);
			ci.cancel();
		}
	}
}
