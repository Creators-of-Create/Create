package com.simibubi.create.lib.mixin.common;

import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FluidState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.event.FluidPlaceBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;

import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Random;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
	@ModifyArgs(
			method = "spreadTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
			)
	)
	private void create$onReactWithNeighbors(Args args, LevelAccessor level, BlockPos pos, BlockState blockState, Direction direction, FluidState fluidState) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(level, pos, blockState);
		if (newState != null) args.set(1, newState);
	}
}
