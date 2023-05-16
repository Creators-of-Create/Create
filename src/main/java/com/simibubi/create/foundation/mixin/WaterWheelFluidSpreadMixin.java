package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.components.waterwheel.WaterWheelStructuralBlock;

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
	@Inject(method = "canPassThrough(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z", at = @At("HEAD"), cancellable = true)
	protected void create$canPassThroughOnWaterWheel(BlockGetter pLevel, Fluid pFluid, BlockPos pFromPos, BlockState p_75967_,
		Direction pDirection, BlockPos p_75969_, BlockState p_75970_, FluidState p_75971_,
		CallbackInfoReturnable<Boolean> cir) {

		if (pDirection.getAxis() == Axis.Y)
			return;

		BlockPos belowPos = pFromPos.below();
		BlockState belowState = pLevel.getBlockState(belowPos);

		if (AllBlocks.WATER_WHEEL_STRUCTURAL.has(belowState)) {
			if (AllBlocks.WATER_WHEEL_STRUCTURAL.get()
				.stillValid(pLevel, belowPos, belowState, false))
				belowState = pLevel.getBlockState(WaterWheelStructuralBlock.getMaster(pLevel, belowPos, belowState));
		} else if (!AllBlocks.WATER_WHEEL.has(belowState))
			return;

		if (belowState.getBlock() instanceof IRotate irotate
			&& irotate.getRotationAxis(belowState) == pDirection.getAxis())
			cir.setReturnValue(false);
	}
}
