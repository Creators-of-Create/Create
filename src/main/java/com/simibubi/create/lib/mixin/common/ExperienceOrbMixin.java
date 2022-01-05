package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.simibubi.create.lib.block.CustomFrictionBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {
	public ExperienceOrbMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyVariable(
			method = "tick",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/Block;getFriction()F")
	)
	private float create$setFriction(float original) {
		BlockPos pos = new BlockPos(getX(), getY() - 1, getZ());
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof CustomFrictionBlock custom) {
			return custom.getFriction(state, level, pos, this);
		}
		return original;
	}
}
