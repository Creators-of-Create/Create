package com.simibubi.create.foundation.mixin;

import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;

import com.simibubi.create.foundation.mixin.accessor.UseOnContextAccessor;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;

import net.minecraft.world.item.context.BlockPlaceContext;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place", at = @At("HEAD"), cancellable = true)
	private void create$fixDeployerPlacement(BlockPlaceContext pContext, CallbackInfoReturnable<InteractionResult> cir) {
		BlockState state = pContext.getLevel().getBlockState(((UseOnContextAccessor) pContext).create$getHitResult().getBlockPos());
		if (state != Blocks.AIR.defaultBlockState() && pContext.getPlayer() instanceof DeployerFakePlayer) {
			cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
