package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.item.BlockUseBypassingItem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockBehaviour$BlockStateBaseMixin {
	@Inject(at = @At("HEAD"), method = "use", cancellable = true)
	private void create$use(Level level, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
		Item held = player.getItemInHand(hand).getItem();
		BlockPos pos = result.getBlockPos();
		if (held instanceof BlockUseBypassingItem bypassing) {
			if (bypassing.shouldBypass(level.getBlockState(pos), pos, level, player, hand)) cir.setReturnValue(InteractionResult.PASS);
		} else if (held instanceof BlockItem blockItem && blockItem.getBlock() instanceof BlockUseBypassingItem bypassing) {
			if (bypassing.shouldBypass(level.getBlockState(pos), pos, level, player, hand)) cir.setReturnValue(InteractionResult.PASS);
		}
	}
}
