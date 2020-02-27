package com.simibubi.create.modules.contraptions.components.contraptions;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.components.contraptions.chassis.AbstractChassisBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMovementTraits {
	
	public static boolean movementAllowed(World world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		if (blockState.getBlock() instanceof AbstractChassisBlock)
			return true;
		if (blockState.getBlock() instanceof ShulkerBoxBlock)
			return false;
		if (blockState.getBlockHardness(world, pos) == -1)
			return false;
		if (blockState.getBlock() == Blocks.OBSIDIAN)
			return false;
		return blockState.getPushReaction() != PushReaction.BLOCK;
	}
	
	public static boolean notSupportive(BlockState state, Direction facing) {
		if (AllBlocks.DRILL.typeOf(state))
			return state.get(BlockStateProperties.FACING) == facing;
		if (AllBlocks.SAW.typeOf(state))
			return state.get(BlockStateProperties.FACING) == facing;
		if (AllBlocks.HARVESTER.typeOf(state))
			return state.get(BlockStateProperties.HORIZONTAL_FACING) == facing;
		return false;
	}
	
	public static boolean movementIgnored(BlockState state) {
		if (AllBlocks.MECHANICAL_PISTON.typeOf(state))
			return true;
		if (AllBlocks.STICKY_MECHANICAL_PISTON.typeOf(state))
			return true;
		if (AllBlocks.MECHANICAL_PISTON_HEAD.typeOf(state))
			return true;
		return false;
	}

}
