package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.foundation.item.CustomUseEffectsItem;

import net.createmod.catnip.api.data.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class CustomItemUseEffectsMixin extends Entity {
	private CustomItemUseEffectsMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Shadow
	public abstract ItemStack getUseItem();

	@Inject(method = "spawnItemParticles(Lnet/minecraft/world/item/ItemStack;I)V", at = @At("HEAD"), cancellable = true)
	private void create$onSpawnItemParticles(ItemStack stack, int count, CallbackInfo ci) {
		Item item = stack.getItem();
		if (item instanceof CustomUseEffectsItem handler) {
			TriState shouldTrigger = handler.shouldTriggerUseEffects(stack, (LivingEntity) (Object) this);
			if (shouldTrigger == TriState.FALSE) {
				ci.cancel();
				return;
			}
			if (handler.triggerUseEffects(stack, (LivingEntity) (Object) this, count, random)) {
				ci.cancel();
			}
		}
	}
}
