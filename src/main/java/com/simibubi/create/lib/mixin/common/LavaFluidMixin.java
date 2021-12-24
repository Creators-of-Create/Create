package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.event.FluidPlaceBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
	@Redirect(
			method = "randomTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
			)
	)
	private boolean create$randomTick(Level world, BlockPos pos, BlockState state) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(world, pos, state);

		return world.setBlockAndUpdate(pos, newState != null ? newState : state);
	}

	@Redirect(
			method = "spreadTo",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
			)
	)
	private boolean create$spreadTo(LevelAccessor world, BlockPos pos, BlockState state, int flags) {
		BlockState newState = FluidPlaceBlockCallback.EVENT.invoker().onFluidPlaceBlock(world, pos, state);

		return world.setBlock(pos, newState != null ? newState : state, flags);
	}
}
