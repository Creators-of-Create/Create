package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltSlope;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BeltTunnelBlock extends Block implements ITE<BeltTunnelTileEntity>, IWrenchable {

	public static final Property<Shape> SHAPE = EnumProperty.create("shape", Shape.class);
	public static final Property<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	public BeltTunnelBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(SHAPE, Shape.STRAIGHT));
	}

	public enum Shape implements IStringSerializable {
		STRAIGHT, WINDOW, CLOSED, T_LEFT, T_RIGHT, CROSS;

		@Override
		public String getString() {
			return Lang.asId(name());
		}
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return BeltTunnelShapes.getShape(state);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ANDESITE_TUNNEL.create();
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState blockState = worldIn.getBlockState(pos.down());
		if (!isValidPositionForPlacement(state, worldIn, pos))
			return false;
		if (!blockState.get(BeltBlock.CASING))
			return false;
		return true;
	}

	public boolean isValidPositionForPlacement(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState blockState = worldIn.getBlockState(pos.down());
		if (!AllBlocks.BELT.has(blockState))
			return false;
		if (blockState.get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return false;
		return true;
	}

	public static boolean hasWindow(BlockState state) {
		return state.get(SHAPE) == Shape.WINDOW || state.get(SHAPE) == Shape.CLOSED;
	}

	public static boolean isStraight(BlockState state) {
		return hasWindow(state) || state.get(SHAPE) == Shape.STRAIGHT;
	}
	
	public static boolean isJunction(BlockState state) {
		Shape shape = state.get(SHAPE);
		return shape == Shape.CROSS || shape == Shape.T_LEFT || shape == Shape.T_RIGHT;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getTunnelState(context.getWorld(), context.getPos());
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (!(world instanceof WrappedWorld) && !world.isRemote())
			withTileEntityDo(world, pos, BeltTunnelTileEntity::updateTunnelConnections);
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing.getAxis()
			.isVertical())
			return state;
		if (!(worldIn instanceof WrappedWorld) && !worldIn.isRemote())
			withTileEntityDo(worldIn, currentPos, BeltTunnelTileEntity::updateTunnelConnections);
		BlockState tunnelState = getTunnelState(worldIn, currentPos);
		if (tunnelState.get(HORIZONTAL_AXIS) == state.get(HORIZONTAL_AXIS)) {
			if (hasWindow(tunnelState) == hasWindow(state))
				return state;
		}

		return tunnelState;
	}

	public void updateTunnel(IWorld world, BlockPos pos) {
		BlockState tunnel = world.getBlockState(pos);
		BlockState newTunnel = getTunnelState(world, pos);
		if (tunnel != newTunnel) {
			world.setBlockState(pos, newTunnel, 3);
			TileEntity te = world.getTileEntity(pos);
			if (te != null && (te instanceof BeltTunnelTileEntity))
				((BeltTunnelTileEntity) te).updateTunnelConnections();
		}
	}

	private BlockState getTunnelState(IBlockReader reader, BlockPos pos) {
		BlockState state = getDefaultState();
		BlockState belt = reader.getBlockState(pos.down());
		if (AllBlocks.BELT.has(belt))
			state = state.with(HORIZONTAL_AXIS, belt.get(BeltBlock.HORIZONTAL_FACING)
				.getAxis());
		Axis axis = state.get(HORIZONTAL_AXIS);

		// T and Cross
		Direction left = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis)
			.rotateY();
		boolean onLeft = hasValidOutput(reader, pos.down(), left);
		boolean onRight = hasValidOutput(reader, pos.down(), left.getOpposite());

		if (onLeft && onRight)
			state = state.with(SHAPE, Shape.CROSS);
		else if (onLeft)
			state = state.with(SHAPE, Shape.T_LEFT);
		else if (onRight)
			state = state.with(SHAPE, Shape.T_RIGHT);

		if (state.get(SHAPE) == Shape.STRAIGHT) {
			boolean canHaveWindow = canHaveWindow(reader, pos, axis);
			if (canHaveWindow)
				state = state.with(SHAPE, Shape.WINDOW);
		}

		return state;
	}

	protected boolean canHaveWindow(IBlockReader reader, BlockPos pos, Axis axis) {
		Direction fw = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		BlockState blockState1 = reader.getBlockState(pos.offset(fw));
		BlockState blockState2 = reader.getBlockState(pos.offset(fw.getOpposite()));
		
		boolean funnel1 = blockState1.getBlock() instanceof BeltFunnelBlock
			&& blockState1.get(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
			&& blockState1.get(BeltFunnelBlock.HORIZONTAL_FACING) == fw.getOpposite();
		boolean funnel2 = blockState2.getBlock() instanceof BeltFunnelBlock
			&& blockState2.get(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
			&& blockState2.get(BeltFunnelBlock.HORIZONTAL_FACING) == fw;
		
		boolean valid1 = blockState1.getBlock() instanceof BeltTunnelBlock || funnel1;
		boolean valid2 = blockState2.getBlock() instanceof BeltTunnelBlock || funnel2;
		boolean canHaveWindow = valid1 && valid2 && !(funnel1 && funnel2);
		return canHaveWindow;
	}

	private boolean hasValidOutput(IBlockReader world, BlockPos pos, Direction side) {
		BlockState blockState = world.getBlockState(pos.offset(side));
		if (AllBlocks.BELT.has(blockState))
			return blockState.get(BeltBlock.HORIZONTAL_FACING)
				.getAxis() == side.getAxis();
		DirectBeltInputBehaviour behaviour =
			TileEntityBehaviour.get(world, pos.offset(side), DirectBeltInputBehaviour.TYPE);
		return behaviour != null && behaviour.canInsertFromSide(side);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!hasWindow(state))
			return ActionResultType.PASS;

		// Toggle windows
		Shape shape = state.get(SHAPE);
		shape = shape == Shape.CLOSED ? Shape.WINDOW : Shape.CLOSED;
		World world = context.getWorld();
		if (!world.isRemote)
			world.setBlockState(context.getPos(), state.with(SHAPE, shape), 2);
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction fromAxis = Direction.getFacingFromAxis(AxisDirection.POSITIVE, state.get(HORIZONTAL_AXIS));
		Direction rotated = rotation.rotate(fromAxis);

		return state.with(HORIZONTAL_AXIS, rotated.getAxis());
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isRemote)
			return;

		if (fromPos.equals(pos.down())) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_AXIS, SHAPE);
		super.fillStateContainer(builder);
	}

	@Override
	public Class<BeltTunnelTileEntity> getTileEntityClass() {
		return BeltTunnelTileEntity.class;
	}

}
