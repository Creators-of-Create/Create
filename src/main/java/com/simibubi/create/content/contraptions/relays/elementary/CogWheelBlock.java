package com.simibubi.create.content.contraptions.relays.elementary;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerBlock;
import com.simibubi.create.foundation.utility.Iterate;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class CogWheelBlock extends AbstractShaftBlock implements ICogWheel {

	boolean isLarge;

	protected CogWheelBlock(boolean large, Properties properties) {
		super(properties);
		isLarge = large;
	}

	public static CogWheelBlock small(Properties properties) {
		return new CogWheelBlock(false, properties);
	}

	public static CogWheelBlock large(Properties properties) {
		return new CogWheelBlock(true, properties);
	}

	@Override
	public boolean isLargeCog() {
		return isLarge;
	}

	@Override
	public boolean isSmallCog() {
		return !isLarge;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return (isLarge ? AllShapes.LARGE_GEAR : AllShapes.SMALL_GEAR).get(state.get(AXIS));
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		for (Direction facing : Iterate.directions) {
			if (facing.getAxis() == state.get(AXIS))
				continue;

			BlockPos offsetPos = pos.offset(facing);
			BlockState blockState = worldIn.getBlockState(offsetPos);
			if (blockState.has(AXIS) && facing.getAxis() == blockState.get(AXIS))
				continue;

			if (ICogWheel.isLargeCog(blockState) || isLargeCog() && ICogWheel.isSmallCog(blockState))
				return false;
		}
		return true;
	}

	protected Axis getAxisForPlacement(BlockItemUseContext context) {
		if (context.getPlayer() != null && context.getPlayer().isSneaking())
			return context.getFace().getAxis();

		World world = context.getWorld();
		BlockState stateBelow = world.getBlockState(context.getPos().down());

		if (AllBlocks.ROTATION_SPEED_CONTROLLER.has(stateBelow) && isLargeCog())
			return stateBelow.get(SpeedControllerBlock.HORIZONTAL_AXIS) == Axis.X ? Axis.Z : Axis.X;

		BlockPos placedOnPos = context.getPos().offset(context.getFace().getOpposite());
		BlockState placedAgainst = world.getBlockState(placedOnPos);

		Block block = placedAgainst.getBlock();
		if (ICogWheel.isSmallCog(placedAgainst))
			return ((IRotate) block).getRotationAxis(placedAgainst);

		Axis preferredAxis = getPreferredAxis(context);
		return preferredAxis != null ? preferredAxis : context.getFace().getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		boolean shouldWaterlog = context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER;
		return this.getDefaultState()
			.with(AXIS, getAxisForPlacement(context))
			.with(BlockStateProperties.WATERLOGGED, shouldWaterlog);
	}

	@Override
	public float getParticleTargetRadius() {
		return isLargeCog() ? 1.125f : .65f;
	}

	@Override
	public float getParticleInitialRadius() {
		return isLargeCog() ? 1f : .75f;
	}
}
