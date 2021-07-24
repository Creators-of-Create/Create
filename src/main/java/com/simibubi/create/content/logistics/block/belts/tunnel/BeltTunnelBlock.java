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
		registerDefaultState(defaultBlockState().setValue(SHAPE, Shape.STRAIGHT));
	}

	public enum Shape implements IStringSerializable {
		STRAIGHT, WINDOW, CLOSED, T_LEFT, T_RIGHT, CROSS;

		@Override
		public String getSerializedName() {
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
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState blockState = worldIn.getBlockState(pos.below());
		if (!isValidPositionForPlacement(state, worldIn, pos))
			return false;
		if (!blockState.getValue(BeltBlock.CASING))
			return false;
		return true;
	}

	public boolean isValidPositionForPlacement(BlockState state, IWorldReader worldIn, BlockPos pos) {
		BlockState blockState = worldIn.getBlockState(pos.below());
		if (!AllBlocks.BELT.has(blockState))
			return false;
		if (blockState.getValue(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL)
			return false;
		return true;
	}

	public static boolean hasWindow(BlockState state) {
		return state.getValue(SHAPE) == Shape.WINDOW || state.getValue(SHAPE) == Shape.CLOSED;
	}

	public static boolean isStraight(BlockState state) {
		return hasWindow(state) || state.getValue(SHAPE) == Shape.STRAIGHT;
	}
	
	public static boolean isJunction(BlockState state) {
		Shape shape = state.getValue(SHAPE);
		return shape == Shape.CROSS || shape == Shape.T_LEFT || shape == Shape.T_RIGHT;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getTunnelState(context.getLevel(), context.getClickedPos());
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState p_220082_4_, boolean p_220082_5_) {
		if (!(world instanceof WrappedWorld) && !world.isClientSide())
			withTileEntityDo(world, pos, BeltTunnelTileEntity::updateTunnelConnections);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (facing.getAxis()
			.isVertical())
			return state;
		if (!(worldIn instanceof WrappedWorld) && !worldIn.isClientSide())
			withTileEntityDo(worldIn, currentPos, BeltTunnelTileEntity::updateTunnelConnections);
		BlockState tunnelState = getTunnelState(worldIn, currentPos);
		if (tunnelState.getValue(HORIZONTAL_AXIS) == state.getValue(HORIZONTAL_AXIS)) {
			if (hasWindow(tunnelState) == hasWindow(state))
				return state;
		}

		return tunnelState;
	}

	public void updateTunnel(IWorld world, BlockPos pos) {
		BlockState tunnel = world.getBlockState(pos);
		BlockState newTunnel = getTunnelState(world, pos);
		if (tunnel != newTunnel && !world.isClientSide()) {
			world.setBlock(pos, newTunnel, 3);
			TileEntity te = world.getBlockEntity(pos);
			if (te != null && (te instanceof BeltTunnelTileEntity))
				((BeltTunnelTileEntity) te).updateTunnelConnections();
		}
	}

	private BlockState getTunnelState(IBlockReader reader, BlockPos pos) {
		BlockState state = defaultBlockState();
		BlockState belt = reader.getBlockState(pos.below());
		if (AllBlocks.BELT.has(belt))
			state = state.setValue(HORIZONTAL_AXIS, belt.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxis());
		Axis axis = state.getValue(HORIZONTAL_AXIS);

		// T and Cross
		Direction left = Direction.get(AxisDirection.POSITIVE, axis)
			.getClockWise();
		boolean onLeft = hasValidOutput(reader, pos.below(), left);
		boolean onRight = hasValidOutput(reader, pos.below(), left.getOpposite());

		if (onLeft && onRight)
			state = state.setValue(SHAPE, Shape.CROSS);
		else if (onLeft)
			state = state.setValue(SHAPE, Shape.T_LEFT);
		else if (onRight)
			state = state.setValue(SHAPE, Shape.T_RIGHT);

		if (state.getValue(SHAPE) == Shape.STRAIGHT) {
			boolean canHaveWindow = canHaveWindow(reader, pos, axis);
			if (canHaveWindow)
				state = state.setValue(SHAPE, Shape.WINDOW);
		}

		return state;
	}

	protected boolean canHaveWindow(IBlockReader reader, BlockPos pos, Axis axis) {
		Direction fw = Direction.get(AxisDirection.POSITIVE, axis);
		BlockState blockState1 = reader.getBlockState(pos.relative(fw));
		BlockState blockState2 = reader.getBlockState(pos.relative(fw.getOpposite()));
		
		boolean funnel1 = blockState1.getBlock() instanceof BeltFunnelBlock
			&& blockState1.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
			&& blockState1.getValue(BeltFunnelBlock.HORIZONTAL_FACING) == fw.getOpposite();
		boolean funnel2 = blockState2.getBlock() instanceof BeltFunnelBlock
			&& blockState2.getValue(BeltFunnelBlock.SHAPE) == BeltFunnelBlock.Shape.EXTENDED
			&& blockState2.getValue(BeltFunnelBlock.HORIZONTAL_FACING) == fw;
		
		boolean valid1 = blockState1.getBlock() instanceof BeltTunnelBlock || funnel1;
		boolean valid2 = blockState2.getBlock() instanceof BeltTunnelBlock || funnel2;
		boolean canHaveWindow = valid1 && valid2 && !(funnel1 && funnel2);
		return canHaveWindow;
	}

	private boolean hasValidOutput(IBlockReader world, BlockPos pos, Direction side) {
		BlockState blockState = world.getBlockState(pos.relative(side));
		if (AllBlocks.BELT.has(blockState))
			return blockState.getValue(BeltBlock.HORIZONTAL_FACING)
				.getAxis() == side.getAxis();
		DirectBeltInputBehaviour behaviour =
			TileEntityBehaviour.get(world, pos.relative(side), DirectBeltInputBehaviour.TYPE);
		return behaviour != null && behaviour.canInsertFromSide(side);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (!hasWindow(state))
			return ActionResultType.PASS;

		// Toggle windows
		Shape shape = state.getValue(SHAPE);
		shape = shape == Shape.CLOSED ? Shape.WINDOW : Shape.CLOSED;
		World world = context.getLevel();
		if (!world.isClientSide)
			world.setBlock(context.getClickedPos(), state.setValue(SHAPE, shape), 2);
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction fromAxis = Direction.get(AxisDirection.POSITIVE, state.getValue(HORIZONTAL_AXIS));
		Direction rotated = rotation.rotate(fromAxis);

		return state.setValue(HORIZONTAL_AXIS, rotated.getAxis());
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		if (fromPos.equals(pos.below())) {
			if (!canSurvive(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_AXIS, SHAPE);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public Class<BeltTunnelTileEntity> getTileEntityClass() {
		return BeltTunnelTileEntity.class;
	}

}
