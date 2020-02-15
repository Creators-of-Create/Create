package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.modules.contraptions.RotationPropagator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class HorizontalAxisKineticBlock extends KineticBlock {

	public static final IProperty<Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;

	public HorizontalAxisKineticBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_AXIS);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Axis preferredAxis = getPreferredHorizontalAxis(context);
		if (preferredAxis != null)
			return this.getDefaultState().with(HORIZONTAL_AXIS, preferredAxis);
		return this.getDefaultState().with(HORIZONTAL_AXIS, context.getPlacementHorizontalFacing().rotateY().getAxis());
	}

	public Axis getPreferredHorizontalAxis(BlockItemUseContext context) {
		Direction prefferedSide = null;
		for (Direction side : Direction.values()) {
			if (side.getAxis().isVertical())
				continue;
			BlockState blockState = context.getWorld().getBlockState(context.getPos().offset(side));
			if (blockState.getBlock() instanceof IRotate) {
				if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getWorld(), context.getPos().offset(side),
						blockState, side.getOpposite()))
					if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
						prefferedSide = null;
						break;
					} else {
						prefferedSide = side;
					}
			}
		}
		return prefferedSide == null ? null : prefferedSide.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_AXIS);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(HORIZONTAL_AXIS);
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		Direction facing = context.getFace();
		if (facing.getAxis().isVertical())
			return ActionResultType.PASS;
		World world = context.getWorld();
		if (facing.getAxis() == state.get(HORIZONTAL_AXIS))
			return ActionResultType.PASS;
		if (!world.isRemote) {
			BlockPos pos = context.getPos();
			KineticTileEntity tileEntity = (KineticTileEntity) world.getTileEntity(pos);
			if (tileEntity.hasNetwork()) 
				tileEntity.getNetwork().remove(tileEntity);
			RotationPropagator.handleRemoved(world, pos, tileEntity);
			tileEntity.removeSource();
			world.setBlockState(pos, state.cycle(HORIZONTAL_AXIS), 3);
			tileEntity.attachKinetics();
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		Axis axis = state.get(HORIZONTAL_AXIS);
		return state.with(HORIZONTAL_AXIS,
				rot.rotate(Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis)).getAxis());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state;
	}

}
