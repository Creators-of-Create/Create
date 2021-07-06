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

public abstract class AbstractBellBlock<TE extends AbstractBellTileEntity> extends BellBlock implements ITE<TE> {

	public AbstractBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	@Nullable
	public TileEntity createNewTileEntity(IBlockReader block) {
		return null;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext selection) {
		Direction facing = state.get(field_220133_a);
		switch (state.get(field_220134_b)) {
		case CEILING:
			return AllShapes.BELL_CEILING.get(facing);
		case DOUBLE_WALL:
			return AllShapes.BELL_DOUBLE_WALL.get(facing);
		case FLOOR:
			return AllShapes.BELL_FLOOR.get(facing);
		case SINGLE_WALL:
			return AllShapes.BELL_WALL.get(facing);
		default:
			return VoxelShapes.fullCube();
		}
	}

	@Override
	public boolean ring(World world, BlockState state, BlockRayTraceResult hit, @Nullable PlayerEntity player,
		boolean flag) {
		BlockPos pos = hit.getPos();
		Direction direction = hit.getFace();
		if (direction == null)
			direction = world.getBlockState(pos)
				.get(field_220133_a);

		if (!this.canRingFrom(state, direction, hit.getHitVec().y - pos.getY()))
			return false;

		TE te = getTileEntity(world, pos);
		if (te == null || !te.ring(world, pos, direction))
			return false;

		if (!world.isRemote) {
			playSound(world, pos);
			if (player != null)
				player.addStat(Stats.BELL_RING);
		}

		return true;
	}

	public boolean canRingFrom(BlockState state, Direction hitDir, double heightChange) {
		if (hitDir.getAxis() == Direction.Axis.Y)
			return false;
		if (heightChange > 0.8124)
			return false;

		Direction direction = state.get(field_220133_a);
		BellAttachment bellAttachment = state.get(field_220134_b);
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
