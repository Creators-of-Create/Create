package com.simibubi.create.content.contraptions.components.waterwheel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaterWheelBlock extends HorizontalKineticBlock implements ITE<WaterWheelTileEntity> {

	public WaterWheelBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.WATER_WHEEL.create();
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbourPos = pos.offset(direction);
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!AllBlocks.WATER_WHEEL.has(neighbourState))
				continue;
			if (neighbourState.get(HORIZONTAL_FACING)
				.getAxis() != state.get(HORIZONTAL_FACING)
					.getAxis()
				|| state.get(HORIZONTAL_FACING)
					.getAxis() != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (worldIn instanceof WrappedWorld)
			return stateIn;
		updateFlowAt(stateIn, worldIn, currentPos, facing);
		updateWheelSpeed(worldIn, currentPos);
		return stateIn;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateAllSides(state, worldIn, pos);
	}

	public void updateAllSides(BlockState state, World worldIn, BlockPos pos) {
		for (Direction d : Direction.values())
			updateFlowAt(state, worldIn, pos, d);
		updateWheelSpeed(worldIn, pos);
	}

	private void updateFlowAt(BlockState state, IWorld world, BlockPos pos, Direction f) {
		if (f.getAxis() == state.get(HORIZONTAL_FACING)
			.getAxis())
			return;

		FluidState fluid = world.getFluidState(pos.offset(f));
		Direction wf = state.get(HORIZONTAL_FACING);
		boolean clockwise = wf.getAxisDirection() == AxisDirection.POSITIVE;
		int clockwiseMultiplier = 2;

		Vector3d vec = fluid.getFlow(world, pos.offset(f));
		vec = vec.scale(f.getAxisDirection()
			.getOffset());
		vec = new Vector3d(Math.signum(vec.x), Math.signum(vec.y), Math.signum(vec.z));
		Vector3d flow = vec;

		withTileEntityDo(world, pos, te -> {
			double flowStrength = 0;

			if (wf.getAxis() == Axis.Z) {
				if (f.getAxis() == Axis.Y)
					flowStrength = flow.x > 0 ^ !clockwise ? -flow.x * clockwiseMultiplier : -flow.x;
				if (f.getAxis() == Axis.X)
					flowStrength = flow.y < 0 ^ !clockwise ? flow.y * clockwiseMultiplier : flow.y;
			}

			if (wf.getAxis() == Axis.X) {
				if (f.getAxis() == Axis.Y)
					flowStrength = flow.z < 0 ^ !clockwise ? flow.z * clockwiseMultiplier : flow.z;
				if (f.getAxis() == Axis.Z)
					flowStrength = flow.y > 0 ^ !clockwise ? -flow.y * clockwiseMultiplier : -flow.y;
			}

			if (te.getSpeed() == 0 && flowStrength != 0 && !world.isRemote()) {
				AllTriggers.triggerForNearbyPlayers(AllTriggers.WATER_WHEEL, world, pos, 5);
				if (fluid.getFluid() == Fluids.FLOWING_LAVA || fluid.getFluid() == Fluids.LAVA)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.LAVA_WHEEL, world, pos, 5);
			}

			te.setFlow(f, (float) (flowStrength * AllConfigs.SERVER.kinetics.waterWheelSpeed.get() / 2f));
		});
	}

	private void updateWheelSpeed(IWorld world, BlockPos pos) {
		withTileEntityDo(world, pos, WaterWheelTileEntity::updateGeneratedRotation);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = context.getFace();
		BlockState placedOn = context.getWorld()
			.getBlockState(context.getPos()
				.offset(facing.getOpposite()));
		if (AllBlocks.WATER_WHEEL.has(placedOn))
			return getDefaultState().with(HORIZONTAL_FACING, placedOn.get(HORIZONTAL_FACING));
		if (facing.getAxis()
			.isHorizontal())
			return getDefaultState().with(HORIZONTAL_FACING, context.getPlayer() != null && context.getPlayer()
				.isSneaking() ? facing.getOpposite() : facing);
		return super.getStateForPlacement(context);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return state.get(HORIZONTAL_FACING)
			.getAxis() == face.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public float getParticleTargetRadius() {
		return 1.125f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 1f;
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}

	@Override
	public Class<WaterWheelTileEntity> getTileEntityClass() {
		return WaterWheelTileEntity.class;
	}

}
