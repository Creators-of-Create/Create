package com.simibubi.create.modules.contraptions.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class DirectionalAxisKineticBlock extends DirectionalKineticBlock {

	public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE = BooleanProperty.create("axis_along_first");

	public DirectionalAxisKineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(AXIS_ALONG_FIRST_COORDINATE);
		super.fillStateContainer(builder);
	}

	protected Direction getFacingForPlacement(BlockItemUseContext context) {
		Direction facing = context.getNearestLookingDirection().getOpposite();
		if (context.getPlayer().isSneaking())
			facing = facing.getOpposite();
		return facing;
	}

	protected boolean getAxisAlignmentForPlacement(BlockItemUseContext context) {
		return context.getPlacementHorizontalFacing().getAxis() == Axis.X;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = getFacingForPlacement(context);
		BlockPos pos = context.getPos();
		World world = context.getWorld();
		boolean alongFirst = false;

		if (facing.getAxis().isHorizontal()) {
			alongFirst = facing.getAxis() == Axis.Z;

			Block blockAbove = world.getBlockState(pos.offset(Direction.UP)).getBlock();
			boolean shaftAbove = blockAbove instanceof IRotate && ((IRotate) blockAbove).hasShaftTowards(world,
					pos.up(), world.getBlockState(pos.up()), Direction.DOWN);
			Block blockBelow = world.getBlockState(pos.offset(Direction.DOWN)).getBlock();
			boolean shaftBelow = blockBelow instanceof IRotate && ((IRotate) blockBelow).hasShaftTowards(world,
					pos.down(), world.getBlockState(pos.down()), Direction.UP);

			if (shaftAbove || shaftBelow)
				alongFirst = facing.getAxis() == Axis.X;
		}

		if (facing.getAxis().isVertical()) {
			alongFirst = getAxisAlignmentForPlacement(context);
			Direction prefferedSide = null;
			for (Direction side : Direction.values()) {
				if (side.getAxis().isVertical())
					continue;
				BlockState blockState = context.getWorld().getBlockState(context.getPos().offset(side));
				if (blockState.getBlock() instanceof IRotate) {
					if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getWorld(),
							context.getPos().offset(side), blockState, side.getOpposite()))
						if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
							prefferedSide = null;
							break;
						} else {
							prefferedSide = side;
						}
				}
			}
			if (prefferedSide != null) {
				alongFirst = prefferedSide.getAxis() == Axis.X;
			}
		}

		return this.getDefaultState().with(FACING, facing).with(AXIS_ALONG_FIRST_COORDINATE, alongFirst);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		World world = context.getWorld();
		Direction face = context.getFace();
		if ((turnBackOnWrenched() ? face.getOpposite() : face) == state.get(FACING)) {
			KineticTileEntity.switchToBlockState(world, context.getPos(), state.cycle(AXIS_ALONG_FIRST_COORDINATE));
			return ActionResultType.SUCCESS;
		}
		return super.onWrenched(state, context);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		Axis pistonAxis = state.get(FACING).getAxis();
		boolean alongFirst = state.get(AXIS_ALONG_FIRST_COORDINATE);

		if (pistonAxis == Axis.X)
			return alongFirst ? Axis.Y : Axis.Z;
		if (pistonAxis == Axis.Y)
			return alongFirst ? Axis.X : Axis.Z;
		if (pistonAxis == Axis.Z)
			return alongFirst ? Axis.X : Axis.Y;

		return super.getRotationAxis(state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		if (rot.ordinal() % 2 == 1)
			state = state.cycle(AXIS_ALONG_FIRST_COORDINATE);
		return super.rotate(state, rot);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

}
