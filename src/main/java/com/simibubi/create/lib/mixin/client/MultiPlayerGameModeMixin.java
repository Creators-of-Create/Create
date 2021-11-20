package com.simibubi.create.lib.mixin.client;

import com.simibubi.create.lib.item.BlockUseBypassingItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.item.UseFirstBehaviorItem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Final
	@Shadow
	private ClientPacketListener connection;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"),
			method = "useItemOn",
			cancellable = true)
	public void create$useItemOn(LocalPlayer clientPlayerEntity, ClientLevel clientWorld, InteractionHand hand, BlockHitResult blockRayTraceResult, CallbackInfoReturnable<InteractionResult> cir) {
		if (clientPlayerEntity.getItemInHand(hand).getItem() instanceof UseFirstBehaviorItem first) {
			UseOnContext ctx = new UseOnContext(clientPlayerEntity, hand, blockRayTraceResult);
			InteractionResult result = first.onItemUseFirst(clientPlayerEntity.getItemInHand(hand), ctx);
			if (result != InteractionResult.PASS) {
				this.connection.send(new ServerboundUseItemOnPacket(hand, blockRayTraceResult));
				cir.setReturnValue(result);
			}
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"),
			method = "useItemOn")
	public InteractionResult create$bypassBlockUse(BlockState instance, Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (player.getItemInHand(interactionHand).getItem() instanceof BlockUseBypassingItem bypassing) {
			if (bypassing.shouldBypass(instance, blockHitResult.getBlockPos(), level, player, interactionHand)) return InteractionResult.PASS;
		}
		return instance.use(level, player, interactionHand, blockHitResult);
	}
}
