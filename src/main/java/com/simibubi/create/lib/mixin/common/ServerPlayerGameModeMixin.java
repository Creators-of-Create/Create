package com.simibubi.create.lib.mixin.common;

import com.simibubi.create.lib.item.BlockUseBypassingItem;

import net.minecraft.server.level.ServerPlayerGameMode;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
}
