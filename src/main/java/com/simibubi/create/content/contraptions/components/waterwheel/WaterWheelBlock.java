package com.simibubi.create.content.contraptions.components.waterwheel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class WaterWheelBlock extends DirectionalKineticBlock implements ITE<WaterWheelTileEntity> {

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
			if (neighbourState.get(FACING)
				.getAxis() != state.get(FACING)
					.getAxis()
				|| state.get(FACING)
					.getAxis() != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		World world = worldIn.getWorld();
		if (world == null || worldIn instanceof WrappedWorld)
			return stateIn;
		updateFlowAt(stateIn, world, currentPos, facing);
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

	private void updateFlowAt(BlockState state, World world, BlockPos pos, Direction side) {
		if (side.getAxis() == state.get(FACING)
				.getAxis())
			return;

		IFluidState fluid = world.getFluidState(pos.offset(side));
		Direction wf = state.get(FACING);
		boolean clockwise = wf.getAxisDirection() == AxisDirection.POSITIVE;
		int clockwiseMultiplier = 2;

		Vec3d vec = fluid.getFlow(world, pos.offset(side));
		if (wf.getAxis()
				.isHorizontal()
			&& side.getAxis()
				.isHorizontal()) {
			BlockState adjacentBlock = world.getBlockState(pos.offset(side));
			if (adjacentBlock.getBlock() == Blocks.BUBBLE_COLUMN.getBlock())
				vec = new Vec3d(0, adjacentBlock.get(BubbleColumnBlock.DRAG) ? -1 : 1, 0);
		}

		vec = vec.scale(side.getAxisDirection()
			.getOffset());
		vec = new Vec3d(Math.signum(vec.x), Math.signum(vec.y), Math.signum(vec.z));
		Vec3d flow = vec;

		withTileEntityDo(world, pos, te -> {
			double flowStrength = 0;

			if (wf.getAxis() == Axis.Z) {
				if (side.getAxis() == Axis.Y)
					flowStrength = flow.x > 0 ^ !clockwise ? -flow.x * clockwiseMultiplier : -flow.x;
				if (side.getAxis() == Axis.X)
					flowStrength = flow.y < 0 ^ !clockwise ? flow.y * clockwiseMultiplier : flow.y;
			}

			if (wf.getAxis() == Axis.X) {
				if (side.getAxis() == Axis.Y)
					flowStrength = flow.z < 0 ^ !clockwise ? flow.z * clockwiseMultiplier : flow.z;
				if (side.getAxis() == Axis.Z)
					flowStrength = flow.y > 0 ^ !clockwise ? -flow.y * clockwiseMultiplier : -flow.y;
			}

			if (wf.getAxis() == Axis.Y) {
				if (side.getAxis() == Axis.Z)
					flowStrength = flow.x < 0 ^ !clockwise ? flow.x * clockwiseMultiplier : flow.x;
				if (side.getAxis() == Axis.X)
					flowStrength = flow.z > 0 ^ !clockwise ? -flow.z * clockwiseMultiplier : -flow.z;
			}

			if (te.getSpeed() == 0 && flowStrength != 0 && !world.isRemote) {
				AllTriggers.triggerForNearbyPlayers(AllTriggers.WATER_WHEEL, world, pos, 5);
				if (fluid.getFluid() == Fluids.FLOWING_LAVA || fluid.getFluid() == Fluids.LAVA)
					AllTriggers.triggerForNearbyPlayers(AllTriggers.LAVA_WHEEL, world, pos, 5);
			}

			Integer flowModifier = AllConfigs.SERVER.kinetics.waterWheelFlowSpeed.get();
			te.setFlow(side, (float) (flowStrength * flowModifier / 2f));
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
			return getDefaultState().with(FACING, placedOn.get(FACING));
		if (facing.getAxis()
			.isHorizontal())
			return getDefaultState().with(FACING, context.getPlayer() != null && context.getPlayer()
				.isSneaking() ? facing.getOpposite() : facing);
		return super.getStateForPlacement(context);
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return state.get(FACING)
			.getAxis() == face.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(FACING)
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
