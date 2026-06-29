package com.simibubi.create.foundation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.content.contraptions.actors.seat.SeatBlock;
import com.simibubi.create.content.equipment.armor.NetheriteDivingHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = Entity.class, priority = 1500)
public abstract class EntityMixin {
	@Shadow
	public abstract BlockPos getOnPos();

	@Shadow
	private Level level;

	@Shadow
	public abstract BlockState getBlockStateOn();

	@ModifyReturnValue(method = "fireImmune()Z", at = @At("RETURN"))
	private boolean create$onFireImmune(boolean original) {
		return ((Entity) (Object) this).getPersistentData().getBoolean(NetheriteDivingHandler.FIRE_IMMUNE_KEY) || original;
	}

	@ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getOnPosLegacy()Lnet/minecraft/core/BlockPos;"))
	private BlockPos create$fixSeatBouncing(BlockPos original) {
		return getBlockStateOn().getBlock() instanceof SeatBlock ? getOnPos() : original;
	}
}
