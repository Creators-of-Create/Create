package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.foundation.utility.BeaconHelper;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

@Mixin(BeaconTileEntity.class)
public abstract class LightHandlersAreSolidToBeaconsMixin {
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getOpacity(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)I"), method = "tick()V")
	private int getCorrectedOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return BeaconHelper.getCorrectedOpacity(state, world, pos);
	}
}
