package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.block.AbstractBlock.Properties;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedstoneContactBlock extends ProperDirectionalBlock {

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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
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
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
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
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == this && newState.getBlock() == this) {
			if (state == newState.cycle(POWERED))
				worldIn.updateNeighborsAt(pos, this);
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean hasValidContact = hasValidContact(worldIn, pos, state.getValue(FACING));
		if (state.getValue(POWERED) != hasValidContact)
			worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, hasValidContact));
	}

	public static boolean hasValidContact(IWorld world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos.relative(direction));
		return AllBlocks.REDSTONE_CONTACT.has(blockState) && blockState.getValue(FACING) == direction.getOpposite();
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return state.getValue(POWERED);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
		if (side == null)
			return true;
		return state.getValue(FACING) != side.getOpposite();
	}

	@Override
	public int getSignal(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return state.getValue(POWERED) ? 15 : 0;
	}

}
