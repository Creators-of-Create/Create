package com.simibubi.create.content.decoration;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public class TrainTrapdoorBlock extends TrapDoorBlock implements IWrenchable {
	/**
	 * @deprecated <p> Use {@link TrainTrapdoorBlock#TrainTrapdoorBlock(BlockSetType, Properties)} instead.
	 */
	@ScheduledForRemoval(inVersion = "1.21.1+ Port")
	@Deprecated(since = "6.0.7", forRemoval = true)
	public TrainTrapdoorBlock(Properties properties) {
		super(SlidingDoorBlock.TRAIN_SET_TYPE.get(), properties);
	}

	public TrainTrapdoorBlock(BlockSetType type, Properties properties) {
		super(type, properties);
	}

	public static TrainTrapdoorBlock metal(Properties properties) {
		return new TrainTrapdoorBlock(SlidingDoorBlock.TRAIN_SET_TYPE.get(), properties);
	}

	public static TrainTrapdoorBlock glass(Properties properties) {
		return new TrainTrapdoorBlock(SlidingDoorBlock.GLASS_SET_TYPE.get(), properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		state = state.cycle(OPEN);
		level.setBlock(pos, state, UPDATE_CLIENTS);
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		playSound(player, level, pos, state.getValue(OPEN));
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState other, Direction pDirection) {
		return state.is(this) == other.is(this) && isConnected(state, other, pDirection);
	}

	public static boolean isConnected(BlockState state, BlockState other, Direction pDirection) {
		state = state.setValue(WATERLOGGED, false)
			.setValue(POWERED, false);
		other = other.setValue(WATERLOGGED, false)
			.setValue(POWERED, false);

		boolean open = state.getValue(OPEN);
		Half half = state.getValue(HALF);
		Direction facing = state.getValue(FACING);

		if (open != other.getValue(OPEN))
			return false;
		if (!open && half == other.getValue(HALF))
			return pDirection.getAxis() != Axis.Y;
		if (!open && half != other.getValue(HALF) && pDirection.getAxis() == Axis.Y)
			return true;
		if (open && facing.getOpposite() == other.getValue(FACING) && pDirection.getAxis() == facing.getAxis())
			return true;
		if ((open ? state.setValue(HALF, Half.TOP) : state) != (open ? other.setValue(HALF, Half.TOP) : other))
			return false;

		return pDirection.getAxis() != facing.getAxis();
	}
}
