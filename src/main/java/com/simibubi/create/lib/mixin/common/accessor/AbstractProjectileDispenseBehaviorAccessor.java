package com.simibubi.create.lib.mixin.common.accessor;

import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractProjectileDispenseBehavior.class)
public interface AbstractProjectileDispenseBehaviorAccessor {
	@Invoker("getProjectile")
	Projectile create$getProjectile(Level level, Position position, ItemStack stack);

	@Invoker("getUncertainty")
	float create$getUncertainty();

	@Invoker("getPower")
	float create$getPower();
}
