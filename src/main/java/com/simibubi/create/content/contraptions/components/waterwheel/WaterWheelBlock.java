package com.simibubi.create.content.contraptions.components.waterwheel;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WaterWheelBlock extends DirectionalKineticBlock implements ITE<WaterWheelTileEntity> {

	public WaterWheelBlock(Properties properties) {
		super(properties);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		for (Direction direction : Iterate.directions) {
			BlockPos neighbourPos = pos.relative(direction);
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!AllBlocks.WATER_WHEEL.has(neighbourState))
				continue;
			if (neighbourState.getValue(FACING)
				.getAxis() != state.getValue(FACING)
					.getAxis()
				|| state.getValue(FACING)
					.getAxis() != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		if (worldIn instanceof WrappedWorld)
			return stateIn;
		updateFlowAt(stateIn, worldIn, currentPos, facing);
		updateWheelSpeed(worldIn, currentPos);
		return stateIn;
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);
		updateAllSides(state, worldIn, pos);
	}

	public void updateAllSides(BlockState state, Level worldIn, BlockPos pos) {
		for (Direction d : Iterate.directions)
			updateFlowAt(state, worldIn, pos, d);
		updateWheelSpeed(worldIn, pos);
	}

	private void updateFlowAt(BlockState state, LevelAccessor world, BlockPos pos, Direction side) {
		if (side.getAxis() == state.getValue(FACING)
			.getAxis())
			return;

		FluidState fluid = world.getFluidState(pos.relative(side));
		Direction wf = state.getValue(FACING);
		boolean clockwise = wf.getAxisDirection() == AxisDirection.POSITIVE;
		int clockwiseMultiplier = 2;

		Vec3 vec = fluid.getFlow(world, pos.relative(side));
		if (side.getAxis()
			.isHorizontal()) {
			BlockState adjacentBlock = world.getBlockState(pos.relative(side));
			if (adjacentBlock.getBlock() == Blocks.BUBBLE_COLUMN)
				vec = new Vec3(0, adjacentBlock.getValue(BubbleColumnBlock.DRAG_DOWN) ? -1 : 1, 0);
		}

		vec = vec.scale(side.getAxisDirection()
			.getStep());
		vec = new Vec3(Math.signum(vec.x), Math.signum(vec.y), Math.signum(vec.z));
		Vec3 flow = vec;

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

			if (te.getSpeed() == 0 && flowStrength != 0 && !world.isClientSide())
				te.award(
					FluidHelper.isLava(fluid.getType()) ? AllAdvancements.LAVA_WHEEL : AllAdvancements.WATER_WHEEL);

			Integer flowModifier = AllConfigs.SERVER.kinetics.waterWheelFlowSpeed.get();
			te.setFlow(side, (float) (flowStrength * flowModifier / 2f));
		});
	}

	private void updateWheelSpeed(LevelAccessor world, BlockPos pos) {
		withTileEntityDo(world, pos, WaterWheelTileEntity::updateGeneratedRotation);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		Direction horizontalFacing = context.getHorizontalDirection();
		BlockPos pos = context.getClickedPos();
		Level world = context.getLevel();
		Player player = context.getPlayer();

		BlockState placedOn = world.getBlockState(pos.relative(face.getOpposite()));
		if (AllBlocks.WATER_WHEEL.has(placedOn))
			return defaultBlockState().setValue(FACING, placedOn.getValue(FACING));

		Direction facing = face;
		boolean sneaking = player != null && player.isShiftKeyDown();
		if (player != null) {

			Vec3 lookVec = player.getLookAngle();
			double tolerance = 0.985;

			if (!canSurvive(defaultBlockState().setValue(FACING, Direction.UP), world, pos))
				facing = horizontalFacing;
			else if (Vec3.atLowerCornerOf(Direction.DOWN.getNormal())
				.dot(lookVec.normalize()) > tolerance)
				facing = Direction.DOWN;
			else if (Vec3.atLowerCornerOf(Direction.UP.getNormal())
				.dot(lookVec.normalize()) > tolerance)
				facing = Direction.UP;
			else
				facing = horizontalFacing;

		}

		return defaultBlockState().setValue(FACING, sneaking ? facing.getOpposite() : facing);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return state.getValue(FACING)
			.getAxis() == face.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
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

	@Override
	public BlockEntityType<? extends WaterWheelTileEntity> getTileEntityType() {
		return AllTileEntities.WATER_WHEEL.get();
	}

	public static Couple<Integer> getSpeedRange() {
		Integer base = AllConfigs.SERVER.kinetics.waterWheelBaseSpeed.get();
		Integer flow = AllConfigs.SERVER.kinetics.waterWheelFlowSpeed.get();
		return Couple.create(base, base + 4 * flow);
	}

}
