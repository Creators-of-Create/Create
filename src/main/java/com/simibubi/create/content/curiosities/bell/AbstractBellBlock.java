package com.simibubi.create.content.curiosities.bell;

import javax.annotation.Nullable;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BellAttachment;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public abstract class AbstractBellBlock<TE extends AbstractBellTileEntity> extends BellBlock implements ITE<TE> {

	public AbstractBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	@Nullable
	public TileEntity newBlockEntity(IBlockReader block) {
		return null;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext selection) {
		Direction facing = state.getValue(FACING);
		switch (state.getValue(ATTACHMENT)) {
		case CEILING:
			return AllShapes.BELL_CEILING.get(facing);
		case DOUBLE_WALL:
			return AllShapes.BELL_DOUBLE_WALL.get(facing);
		case FLOOR:
			return AllShapes.BELL_FLOOR.get(facing);
		case SINGLE_WALL:
			return AllShapes.BELL_WALL.get(facing);
		default:
			return VoxelShapes.block();
		}
	}

	@Override
	public boolean onHit(World world, BlockState state, BlockRayTraceResult hit, @Nullable PlayerEntity player,
		boolean flag) {
		BlockPos pos = hit.getBlockPos();
		Direction direction = hit.getDirection();
		if (direction == null)
			direction = world.getBlockState(pos)
				.getValue(FACING);

		if (!this.canRingFrom(state, direction, hit.getLocation().y - pos.getY()))
			return false;

		TE te = getTileEntity(world, pos);
		if (te == null || !te.ring(world, pos, direction))
			return false;

		if (!world.isClientSide) {
			playSound(world, pos);
			if (player != null)
				player.awardStat(Stats.BELL_RING);
		}

		return true;
	}

	public boolean canRingFrom(BlockState state, Direction hitDir, double heightChange) {
		if (hitDir.getAxis() == Direction.Axis.Y)
			return false;
		if (heightChange > 0.8124)
			return false;

		Direction direction = state.getValue(FACING);
		BellAttachment bellAttachment = state.getValue(ATTACHMENT);
		switch (bellAttachment) {
		case FLOOR:
		case CEILING:
			return direction.getAxis() == hitDir.getAxis();
		case SINGLE_WALL:
		case DOUBLE_WALL:
			return direction.getAxis() != hitDir.getAxis();
		default:
			return false;
		}
	}

	public abstract void playSound(World world, BlockPos pos);

}
