package com.simibubi.create.lib.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
public interface PlayerAccessor {
	@Invoker("closeContainer")
	void create$closeScreen();
}
