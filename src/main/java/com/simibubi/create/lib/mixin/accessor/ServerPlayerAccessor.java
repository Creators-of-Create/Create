package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.level.ServerPlayer;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
	@Accessor("containerCounter")
	int create$getContainerCounter();
}
