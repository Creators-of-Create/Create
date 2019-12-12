package com.simibubi.create.modules.contraptions.redstone;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.contraptions.IHaveMovementBehavior;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;

public class ContactBlock extends ProperDirectionalBlock implements IHaveMovementBehavior {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public ContactBlock() {
		super(Properties.from(Blocks.ANDESITE));
		setDefaultState(getDefaultState().with(POWERED, false).with(FACING, Direction.UP));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(POWERED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
		Direction placeDirection = context.getFace().getOpposite();

		if (context.isPlacerSneaking() || hasValidContact(context.getWorld(), context.getPos(), placeDirection))
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
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		boolean hasValidContact = hasValidContact(worldIn, pos, state.get(FACING));
		if (state.get(POWERED) != hasValidContact)
			worldIn.setBlockState(pos, state.with(POWERED, hasValidContact));
	}

	public static boolean hasValidContact(IWorld world, BlockPos pos, Direction direction) {
		BlockState blockState = world.getBlockState(pos.offset(direction));
		return AllBlocks.CONTACT.typeOf(blockState) && blockState.get(FACING) == direction.getOpposite();
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return state.get(POWERED);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		if (side == null)
			return true;
		return state.get(FACING) != side.getOpposite();
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
		return state.get(POWERED) ? 15 : 0;
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(FACING).getDirectionVec()).scale(.65f);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		BlockState block = context.state;
		World world = context.world;

		if (world.isRemote)
			return;

		BlockState visitedState = world.getBlockState(pos);
		if (!AllBlocks.CONTACT.typeOf(visitedState))
			return;

		Vec3d contact = new Vec3d(block.get(FACING).getDirectionVec());
		contact = VecHelper.rotate(contact, context.rotation.x, context.rotation.y, context.rotation.z);
		Direction direction = Direction.getFacingFromVector(contact.x, contact.y, contact.z);

		if (!hasValidContact(world, pos.offset(direction.getOpposite()), direction))
			return;

		int ticksToStayActive = 4;
		world.setBlockState(pos, visitedState.with(POWERED, true));
		world.getPendingBlockTicks().scheduleTick(pos, this, ticksToStayActive, TickPriority.NORMAL);
		return;
	}

}
