package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.block.HarvestableBlock;
import com.simibubi.create.lib.event.PlayerTickEndCallback;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Player.class)
public abstract class PlayerMixin {
	@Final
	@Shadow
	public Inventory inventory;

	@Inject(at = @At("HEAD"), method = "hasCorrectToolForDrops", cancellable = true)
	public void create$isUsingEffectiveTool(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
		if (blockState.getBlock() instanceof HarvestableBlock && inventory.getSelected().getItem() instanceof DiggerItem) {
			cir.setReturnValue(((HarvestableBlock) blockState.getBlock()).isToolEffective(blockState, (DiggerItem) inventory.getSelected().getItem()));
		}
	}

	@Inject(at = @At("TAIL"), method = "tick()V")
	public void create$clientEndOfTickEvent(CallbackInfo ci) {
		PlayerTickEndCallback.EVENT.invoker().onEndOfPlayerTick(MixinHelper.cast(this));
	}
}
