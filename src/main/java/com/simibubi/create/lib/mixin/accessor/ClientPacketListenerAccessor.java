package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;

@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
	@Accessor("serverChunkRadius")
	int create$getServerChunkRadius();
}
