package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.optics.ILightHandler;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeaconTileEntity.class)
public abstract class LightHandlersAreSolidToBeaconsMixin {
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getOpacity(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)I"), method = "tick()V")
	private int getCorrectedOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		try {
			if (state.getBlock() instanceof ITE && ((ITE<?>) state.getBlock()).getTileEntity(world, pos) instanceof ILightHandler)
				return 15;
		} catch (ITE.TileEntityException ignored) {
		}
		return state.getOpacity(world, pos);
	}
}
