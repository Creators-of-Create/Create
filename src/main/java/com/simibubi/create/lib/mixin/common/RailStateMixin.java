package com.simibubi.create.lib.mixin.common;

import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.simibubi.create.lib.block.SlopeCreationCheckingRailBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.RailState;

@Mixin(RailState.class)
public abstract class RailStateMixin {

	@Final
	@Shadow
	private BaseRailBlock block;

	@Shadow
	private BlockState state;

	@Shadow
	@Final
	private Level level;

	@Redirect(method = { "connectTo", "place" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BaseRailBlock;isRail(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
	private boolean create$redirectRailChecksToCheckSlopes(Level level, BlockPos pos) {
		boolean canMakeSlopes = true;
		if (block instanceof SlopeCreationCheckingRailBlock checking) {
			canMakeSlopes = checking.canMakeSlopes(state, this.level, pos);
		}
		return BaseRailBlock.isRail(level, pos) && canMakeSlopes;
	}

}
