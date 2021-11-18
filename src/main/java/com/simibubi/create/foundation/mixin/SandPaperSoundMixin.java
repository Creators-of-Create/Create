package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.content.curiosities.tools.SandPaperEffectsHandler;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class SandPaperSoundMixin extends Entity {

	@Shadow
	public abstract ItemStack getUseItem();

	private SandPaperSoundMixin(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "triggerItemUseEffects", cancellable = true,
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;", ordinal = 0))
	private void onTriggerUseEffects(ItemStack pStack, int pCount, CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;

		Item item = pStack.getItem();
		if (item instanceof SandPaperItem) {
			SandPaperEffectsHandler.onUseTick(self, this.random);
			ci.cancel();
		}
	}

	// Trigger every tick for sandpaper, so that we have more fine grain control over the animation
	@Inject(method = "shouldTriggerItemUseEffects", cancellable = true, at = @At("HEAD"))
	private void alwaysTriggerUseEffects(CallbackInfoReturnable<Boolean> cir) {
		ItemStack using = this.getUseItem();
		Item item = using.getItem();
		if (item instanceof SandPaperItem) {
			cir.setReturnValue(true);
		}
	}
}
