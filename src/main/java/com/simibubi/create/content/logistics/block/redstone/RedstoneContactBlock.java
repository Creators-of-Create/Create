package com.simibubi.create.content.logistics.block.redstone;

import java.util.Random;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
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

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RedstoneContactBlock extends ProperDirectionalBlock implements IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public RedstoneContactBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(POWERED, false)
			.with(FACING, Direction.UP));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState().with(FACING, context.getNearestLookingDirection()
			.getOpposite());
		Direction placeDirection = context.getFace()
			.getOpposite();

		if ((context.getPlayer() != null && context.getPlayer()
			.isSneaking()) || hasValidContact(context.getWorld(), context.getPos(), placeDirection))
			state = state.with(FACING, placeDirection);
		if (hasValidContact(context.getWorld(), context.getPos(), state.get(FACING)))
			state = state.with(POWERED, true);

		return state;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing != stateIn.get(FACING))
			return stateIn;
		boolean hasValidContact = hasValidContact(worldIn, currentPos, facing);
		if (stateIn.get(POWERED) != hasValidContact) {
			return stateIn.with(POWERED, hasValidContact);
		}
		return stateIn;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() == this && newState.getBlock() == this) {
			if (state == newState.cycle(POWERED))
				worldIn.notifyNeighborsOfStateChange(pos, this);
		}
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		boolean hasValidContact = hasValidContact(worldIn, pos, state.get(FACING));
		if (state.get(POWERED) != hasValidContact)
			worldIn.setBlockState(pos, state.with(POWERED, hasValidContact));
	}

	public static boolean hasValidContact(IWorld world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos.offset(direction));
		return AllBlocks.REDSTONE_CONTACT.has(blockState) && blockState.get(FACING) == direction.getOpposite();
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return state.get(POWERED);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
		if (side == null)
			return true;
		return state.get(FACING) != side.getOpposite();
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return state.get(POWERED) ? 15 : 0;
	}

}
