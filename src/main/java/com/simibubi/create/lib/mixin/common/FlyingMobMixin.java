package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.block.CustomFrictionBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Mixin(FlyingMob.class)
public abstract class FlyingMobMixin extends Mob {
	protected FlyingMobMixin(EntityType<? extends Mob> entityType, Level level) {
		super(entityType, level);
	}

	@Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
	public float create$setSlipperiness(Block instance) {
		if (instance instanceof CustomFrictionBlock custom) {
			BlockPos ground = new BlockPos(getX(), getY() - 1.0D, getZ());

			return custom.getFriction(level.getBlockState(ground), level, ground, this);
		}
		return instance.getFriction();
	}
}
