package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

@Mixin(ClientboundPlayerAbilitiesPacket.class)
public interface ClientboundPlayerAbilitiesPacketAccessor {
	@Accessor("flyingSpeed")
	void create$setFlyingSpeed(float speed);
}
