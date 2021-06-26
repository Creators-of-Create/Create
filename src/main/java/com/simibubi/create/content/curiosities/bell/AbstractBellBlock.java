package com.simibubi.create.content.curiosities.bell;

import javax.annotation.Nullable;

import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
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

	protected VoxelShape getShape(BlockState state) {
		return VoxelShapes.fullCube();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext selection) {
		return this.getShape(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext selection) {
		return this.getShape(state);
	}

	@Override
	public boolean ring(World world, BlockPos pos, @Nullable Direction direction) {
		if (direction == null) {
			direction = world.getBlockState(pos).get(field_220133_a);
		}

		if (!ringInner(world, pos, direction))
			return false;

		if (world.isRemote)
			return false;

		playSound(world, pos, direction);
		return true;
	}

	public static void playSound(World world, BlockPos pos, Direction direction) {
		world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0F, 1.0F);
	}

	protected boolean ringInner(World world, BlockPos pos, Direction direction) {
		TE te = getTileEntity(world, pos);
		return te != null && te.ring(world, pos, direction);
	}
}
