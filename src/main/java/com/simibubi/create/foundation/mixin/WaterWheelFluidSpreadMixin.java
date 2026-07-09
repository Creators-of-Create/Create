package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(FlowingFluid.class)
public class WaterWheelFluidSpreadMixin {
	@Inject(method = "canPassThroughWall", at = @At("HEAD"), cancellable = true)
	private static void create$canPassThroughWallOnWaterWheel(Direction direction, BlockGetter level, BlockPos sourcePos,
		BlockState sourceState, BlockPos targetPos, BlockState targetState, CallbackInfoReturnable<Boolean> cir) {

		create$blockWheelFlow(direction, level, sourcePos, targetPos, cir);
	}

	@Inject(method = "canPassThrough(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z", at = @At("HEAD"), cancellable = true)
	protected void create$canPassThroughOnWaterWheel(BlockGetter pLevel, Fluid pFluid, BlockPos pFromPos, BlockState p_75967_,
		Direction pDirection, BlockPos p_75969_, BlockState p_75970_, FluidState p_75971_,
		CallbackInfoReturnable<Boolean> cir) {

		create$blockWheelFlow(pDirection, pLevel, pFromPos, p_75969_, cir);
	}

	private static void create$blockWheelFlow(Direction pDirection, BlockGetter pLevel, BlockPos pFromPos,
		BlockPos p_75969_, CallbackInfoReturnable<Boolean> cir) {

		if (pDirection.getAxis() == Axis.Y)
			return;

		if (create$blocksHorizontalWheelFlow(pLevel, pFromPos.below(), pDirection)
			|| create$blocksHorizontalWheelFlow(pLevel, p_75969_.below(), pDirection))
			cir.setReturnValue(false);
	}

	private static boolean create$blocksHorizontalWheelFlow(BlockGetter level, BlockPos pos, Direction direction) {
		BlockState belowState = level.getBlockState(pos);

		if (AllBlocks.WATER_WHEEL_STRUCTURAL.has(belowState)) {
			if (AllBlocks.WATER_WHEEL_STRUCTURAL.get()
				.stillValid(level, pos, belowState, false))
				belowState = level.getBlockState(WaterWheelStructuralBlock.getMaster(level, pos, belowState));
		} else if (!AllBlocks.WATER_WHEEL.has(belowState) && !AllBlocks.LARGE_WATER_WHEEL.has(belowState))
			return false;

		return belowState.getBlock() instanceof IRotate irotate
			&& irotate.getRotationAxis(belowState) == direction.getAxis();
	}
}
