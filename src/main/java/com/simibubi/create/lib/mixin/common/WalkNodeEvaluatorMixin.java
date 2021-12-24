package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.simibubi.create.lib.block.CustomPathNodeTypeBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin {
	//TODO: This might not be correct
	@Inject(method = "getBlockPathTypeRaw", at = @At("HEAD"), cancellable = true)
	private static void create$getCommonNodeType(BlockGetter iBlockReader, BlockPos blockPos, CallbackInfoReturnable<BlockPathTypes> cir) {
		Block block = iBlockReader.getBlockState(blockPos).getBlock();
		if (block instanceof CustomPathNodeTypeBlock) {
			cir.setReturnValue(((CustomPathNodeTypeBlock) block).getAiPathNodeType(iBlockReader.getBlockState(blockPos), iBlockReader, blockPos, null));
		}
	}
}
