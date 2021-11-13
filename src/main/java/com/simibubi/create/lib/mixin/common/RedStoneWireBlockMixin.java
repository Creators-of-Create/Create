package com.simibubi.create.lib.mixin.common;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.block.CanConnectRedstoneBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RedStoneWireBlock.class)
public abstract class RedStoneWireBlockMixin {
	// I have concerns for this Shadow but it should be fine? :tiny_potato:
	@Shadow
	protected static boolean shouldConnectTo(BlockState blockState, @Nullable Direction side) {
		return false;
	}

	@Inject(at = @At(value = "RETURN", ordinal = 3),
			method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z", cancellable = true)
	private static void create$canConnectTo(BlockState state, Direction side, CallbackInfoReturnable<Boolean> cir) {
		if (state.getBlock() instanceof CanConnectRedstoneBlock) {
			// Passing null for world and pos here just for extra upstream compat, not properly implementing it because
			// 1. world and pos are never used in Create
			// 2. extra work :help_me:
			cir.setReturnValue(((CanConnectRedstoneBlock) state.getBlock()).canConnectRedstone(state, null, null, side));
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0),
			method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;")
	private boolean create$canConnectUpwardsTo(BlockState state, BlockGetter world, BlockPos pos, Direction side, boolean bl) {
		return shouldConnectTo(state, side);
	}
}
