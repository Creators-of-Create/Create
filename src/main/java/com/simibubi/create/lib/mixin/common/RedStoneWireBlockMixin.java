package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.block.ConnectableRedstoneBlock;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {
	@Inject(
			method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void create$shouldConnectTo(BlockState state, Direction side, CallbackInfoReturnable<Boolean> cir) {
		if (state.getBlock() instanceof ConnectableRedstoneBlock connectable) {
			// Passing null for world and pos here just for extra upstream compat, not properly implementing it because
			// 1. world and pos are never used in Create
			// 2. extra work :help_me:
			cir.setReturnValue(connectable.canConnectRedstone(state, null, null, side));
		}
	}
}
