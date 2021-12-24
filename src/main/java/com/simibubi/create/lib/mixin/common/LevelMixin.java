package com.simibubi.create.lib.mixin.common;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.lib.block.WeakPowerCheckingBlock;
import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.util.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Shadow
	public abstract BlockState getBlockState(BlockPos blockPos);

	@Inject(method = "getSignal", at = @At("RETURN"), cancellable = true)
	public void create$getRedstoneSignal(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Integer> cir) {
		BlockState create$blockstate = MixinHelper.<Level>cast(this).getBlockState(blockPos);
		int create$i = create$blockstate.getSignal(MixinHelper.<Level>cast(this), blockPos, direction);

		if (create$blockstate.getBlock() instanceof WeakPowerCheckingBlock) {
			cir.setReturnValue(
					((WeakPowerCheckingBlock) create$blockstate.getBlock()).shouldCheckWeakPower(create$blockstate, MixinHelper.<Level>cast(this), blockPos, direction)
							? Math.max(create$i, MixinHelper.<Level>cast(this).getDirectSignalTo(blockPos))
							: create$i);
		}
	}

	@Inject(
			method = "updateNeighbourForOutputSignal",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	public void create$updateComparatorOutputLevel(BlockPos blockPos, Block block, CallbackInfo ci,
												   Iterator<?> var3, Direction direction, BlockPos blockPos2) {
		((BlockStateExtensions) getBlockState(blockPos2)).create$onNeighborChange(MixinHelper.cast(this), blockPos2, blockPos);
	}
}
