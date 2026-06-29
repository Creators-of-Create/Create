package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.AllDamageTypes;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

@Mixin(EnchantedCountIncreaseFunction.class)
public abstract class EnchantedCountIncreaseFunctionMixin {
	@Shadow
	@Final
	private NumberProvider value;

	@Shadow
	protected abstract boolean hasLimit();

	@Shadow
	@Final
	private int limit;

	@Inject(method = "run", at = @At("TAIL"))
	private void create$crushingWheelLooting(ItemStack stack, LootContext context, CallbackInfoReturnable<ItemStack> cir) {
		DamageSource damageSource = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
		if (damageSource != null && damageSource.is(AllDamageTypes.CRUSH)) {
			int lootingLevel = 2;

			float f = (float) lootingLevel * this.value.getFloat(context);
			stack.grow(Math.round(f));
			if (this.hasLimit())
				stack.limitSize(this.limit);
		}
	}
}
