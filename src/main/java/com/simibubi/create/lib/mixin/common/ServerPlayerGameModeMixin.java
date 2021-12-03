package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.item.BlockUseBypassingItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.BlockHitResult;

import com.simibubi.create.lib.item.UseFirstBehaviorItem;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"),
			method = "useItemOn")
	public InteractionResult create$bypassBlockUse(BlockState instance, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (player.getItemInHand(interactionHand).getItem() instanceof BlockUseBypassingItem bypassing) {
			if (bypassing.shouldBypass(instance, blockHitResult.getBlockPos(), level, player, interactionHand)) return InteractionResult.PASS;
		}
		return instance.use(level, player, interactionHand, blockHitResult);
	}

	@Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0, shift = At.Shift.BEFORE), cancellable = true)
	public void create$onItemFirstUse(ServerPlayer serverPlayer, Level level, ItemStack itemStack, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
		if (itemStack.getItem() instanceof UseFirstBehaviorItem first) {
			UseOnContext useoncontext = new UseOnContext(serverPlayer, interactionHand, blockHitResult);
			InteractionResult result = first.onItemUseFirst(itemStack, useoncontext);
			if (result != InteractionResult.PASS) cir.setReturnValue(result);
		}
	}
}
