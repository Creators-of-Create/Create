package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.lib.block.CanConnectRedstoneBlock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedstoneContactBlock extends WrenchableDirectionalBlock implements CanConnectRedstoneBlock {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public RedstoneContactBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState().setValue(FACING, context.getNearestLookingDirection()
			.getOpposite());
		Direction placeDirection = context.getClickedFace()
			.getOpposite();

		if ((context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown()) || hasValidContact(context.getLevel(), context.getClickedPos(), placeDirection))
			state = state.setValue(FACING, placeDirection);
		if (hasValidContact(context.getLevel(), context.getClickedPos(), state.getValue(FACING)))
			state = state.setValue(POWERED, true);

		return state;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing != stateIn.getValue(FACING))
			return stateIn;
		boolean hasValidContact = hasValidContact(worldIn, currentPos, facing);
		if (stateIn.getValue(POWERED) != hasValidContact) {
			return stateIn.setValue(POWERED, hasValidContact);
		}
		return stateIn;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == this && newState.getBlock() == this) {
			if (state == newState.cycle(POWERED))
				worldIn.updateNeighborsAt(pos, this);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		boolean hasValidContact = hasValidContact(worldIn, pos, state.getValue(FACING));
		if (state.getValue(POWERED) != hasValidContact)
			worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, hasValidContact));
	}

	public static boolean hasValidContact(LevelAccessor world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos.relative(direction));
		return AllBlocks.REDSTONE_CONTACT.has(blockState) && blockState.getValue(FACING) == direction.getOpposite();
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return state.getValue(POWERED);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side) {
		if (side == null)
			return true;
		return state.getValue(FACING) != side.getOpposite();
	}

	@Override
	public int getSignal(BlockState state, BlockGetter blockAccess, BlockPos pos, Direction side) {
		return state.getValue(POWERED) ? 15 : 0;
	}

}
