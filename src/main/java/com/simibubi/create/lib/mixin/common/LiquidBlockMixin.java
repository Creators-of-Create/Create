package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;

import com.simibubi.create.lib.event.FluidPlaceBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockMixin {
	@ModifyArgs(
			method = "shouldSpreadLiquid",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
			)
	)
	private void create$onReactWithNeighbors(Args args, Level level, BlockPos pos, BlockState state) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(level, pos, state);
		if (newState != null) args.set(1, newState);
	}
}
