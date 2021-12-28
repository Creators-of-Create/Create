package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.block.CustomFrictionBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin extends Entity {
	public ExperienceOrbEntityMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
	public float create$setFriction(Block instance) {
		if (instance instanceof CustomFrictionBlock custom) {
			BlockPos pos = blockPosition();
			BlockState state = level.getBlockState(pos);
			return custom.getFriction(state, level, pos, this);
		}
		return instance.getFriction();
	}
}
