package com.simibubi.create.content.contraptions;

import javax.annotation.Nullable;

import net.createmod.catnip.levelWrappers.WrappedServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class GhostPlacementServerLevel extends WrappedServerLevel {

	protected final Map<BlockPos, BlockState> blockStates = new HashMap<>();
	protected final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

	public GhostPlacementServerLevel(ServerLevel level) {
		super(level);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState newState, int flags) {
		pos = pos.immutable();
		getBlockState(pos).onRemove(this, pos, newState,false);
		blockStates.put(pos, newState);
		return true;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blockStates.getOrDefault(pos, Blocks.AIR.defaultBlockState());
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		BlockState blockState = getBlockState(pos);
		BlockEntity blockEntity = blockEntities.getOrDefault(pos, null);
		if (blockState.hasBlockEntity() && blockEntity == null) {
			blockEntity = ((EntityBlock) blockState.getBlock()).newBlockEntity(pos, blockState);
			if (blockEntity != null)
				blockEntity.setLevel(this);
			pos = pos.immutable();
			blockEntities.put(pos, blockEntity);
		}
		return blockEntity;
	}

	@Override
	public void removeBlockEntity(BlockPos pos) {
		blockEntities.remove(pos);
	}

	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
		BlockState blockState = getBlockState(pos);
		if (blockState.isAir())
			return false;
		if (dropBlock)
			Block.dropResources(blockState, this, pos, getBlockEntity(pos), entity, ItemStack.EMPTY);
		setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_NONE);
		return true;
	}
}
