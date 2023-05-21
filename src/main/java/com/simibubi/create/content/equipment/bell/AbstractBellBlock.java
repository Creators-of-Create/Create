package com.simibubi.create.content.equipment.bell;

import javax.annotation.Nullable;

import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractBellBlock<BE extends AbstractBellBlockEntity> extends BellBlock implements IBE<BE> {

	public AbstractBellBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext selection) {
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
			return Shapes.block();
		}
	}

	@Override
	public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos,
		boolean pIsMoving) {
		if (pLevel.isClientSide)
			return;
		boolean shouldPower = pLevel.hasNeighborSignal(pPos);
		if (shouldPower == pState.getValue(POWERED))
			return;
		pLevel.setBlock(pPos, pState.setValue(POWERED, shouldPower), 3);
		if (!shouldPower)
			return;
		Direction facing = pState.getValue(FACING);
		BellAttachType type = pState.getValue(ATTACHMENT);
		ring(pLevel, pPos,
			type == BellAttachType.CEILING || type == BellAttachType.FLOOR ? facing : facing.getClockWise(), null);
	}

	@Override
	public boolean onHit(Level world, BlockState state, BlockHitResult hit, @Nullable Player player, boolean flag) {
		BlockPos pos = hit.getBlockPos();
		Direction direction = hit.getDirection();
		if (direction == null)
			direction = world.getBlockState(pos)
				.getValue(FACING);
		if (!this.canRingFrom(state, direction, hit.getLocation().y - pos.getY()))
			return false;
		return ring(world, pos, direction, player);
	}

	protected boolean ring(Level world, BlockPos pos, Direction direction, Player player) {
		BE be = getBlockEntity(world, pos);
		if (world.isClientSide)
			return true;
		if (be == null || !be.ring(world, pos, direction))
			return false;
		playSound(world, pos);
		if (player != null)
			player.awardStat(Stats.BELL_RING);
		return true;
	}

	public boolean canRingFrom(BlockState state, Direction hitDir, double heightChange) {
		if (hitDir.getAxis() == Direction.Axis.Y)
			return false;
		if (heightChange > 0.8124)
			return false;

		Direction direction = state.getValue(FACING);
		BellAttachType bellAttachment = state.getValue(ATTACHMENT);
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

	@Nullable
	public BlockEntity newBlockEntity(BlockPos p_152198_, BlockState p_152199_) {
		return IBE.super.newBlockEntity(p_152198_, p_152199_);
	}

	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152194_, BlockState p_152195_,
		BlockEntityType<T> p_152196_) {
		return IBE.super.getTicker(p_152194_, p_152195_, p_152196_);
	}

	public abstract void playSound(Level world, BlockPos pos);

}
