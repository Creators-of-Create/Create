package com.simibubi.create.lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

	@Inject(at = @At("HEAD"),
			method = "useItemOn",
			cancellable = true)
	public void create$func_217292_a(LocalPlayer clientPlayerEntity, ClientLevel clientWorld, InteractionHand hand, BlockHitResult blockRayTraceResult, CallbackInfoReturnable<InteractionResult> cir) {
		if (clientPlayerEntity.getItemInHand(hand).getItem() instanceof UseFirstBehaviorItem) {
			UseOnContext create$itemUseContext = new UseOnContext(clientPlayerEntity, hand, blockRayTraceResult);
			InteractionResult create$result = ((UseFirstBehaviorItem) clientPlayerEntity.getItemInHand(hand).getItem()).onItemUseFirst(clientPlayerEntity.getItemInHand(hand), create$itemUseContext);
			if (create$result != InteractionResult.PASS) {
				this.connection.send(new ServerboundUseItemOnPacket(hand, blockRayTraceResult));
				cir.setReturnValue(create$result);
			}
		}
	}
}
