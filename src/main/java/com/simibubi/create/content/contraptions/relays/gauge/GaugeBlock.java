package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.Random;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GaugeBlock extends DirectionalAxisKineticBlock {

	public static final GaugeShaper GAUGE = GaugeShaper.make();
	protected Type type;

	public enum Type implements StringRepresentable {
		SPEED, STRESS;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	public static GaugeBlock speed(Properties properties) {
		return new GaugeBlock(properties, Type.SPEED);
	}

	public static GaugeBlock stress(Properties properties) {
		return new GaugeBlock(properties, Type.STRESS);
	}

	protected GaugeBlock(Properties properties, Type type) {
		super(properties);
		this.type = type;
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		switch (type) {
		case SPEED:
			return AllTileEntities.SPEEDOMETER.create();
		case STRESS:
			return AllTileEntities.STRESSOMETER.create();
		default:
			return null;
		}
	}

	/*
	 * FIXME: Is there a new way of doing this in 1.16? Or cn we just delete it?
	 *
	 * @SuppressWarnings("deprecation")
	 *
	 * @Override
	 * public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
	 * return Blocks.SPRUCE_PLANKS.getMaterialColor(state, worldIn, pos);
	 * }
	 */

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level world = context.getLevel();
		Direction face = context.getClickedFace();
		BlockPos placedOnPos = context.getClickedPos()
			.relative(context.getClickedFace()
				.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);
		Block block = placedOnState.getBlock();

		if (block instanceof IRotate && ((IRotate) block).hasShaftTowards(world, placedOnPos, placedOnState, face)) {
			BlockState toPlace = defaultBlockState();
			Direction horizontalFacing = context.getHorizontalDirection();
			Direction nearestLookingDirection = context.getNearestLookingDirection();
			boolean lookPositive = nearestLookingDirection.getAxisDirection() == AxisDirection.POSITIVE;
			if (face.getAxis() == Axis.X) {
				toPlace = toPlace.setValue(FACING, lookPositive ? Direction.NORTH : Direction.SOUTH)
					.setValue(AXIS_ALONG_FIRST_COORDINATE, true);
			} else if (face.getAxis() == Axis.Y) {
				toPlace = toPlace.setValue(FACING, horizontalFacing.getOpposite())
					.setValue(AXIS_ALONG_FIRST_COORDINATE, horizontalFacing.getAxis() == Axis.X);
			} else {
				toPlace = toPlace.setValue(FACING, lookPositive ? Direction.WEST : Direction.EAST)
					.setValue(AXIS_ALONG_FIRST_COORDINATE, false);
			}

			return toPlace;
		}

		return super.getStateForPlacement(context);
	}

	@Override
	protected Direction getFacingForPlacement(BlockPlaceContext context) {
		return context.getClickedFace();
	}

	@Override
	protected boolean getAxisAlignmentForPlacement(BlockPlaceContext context) {
		return context.getHorizontalDirection()
			.getAxis() != Axis.X;
	}

	public boolean shouldRenderHeadOnFace(Level world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis()
			.isVertical())
			return false;
		if (face == state.getValue(FACING)
			.getOpposite())
			return false;
		if (face.getAxis() == getRotationAxis(state))
			return false;
		if (getRotationAxis(state) == Axis.Y && face != state.getValue(FACING))
			return false;
		if (!Block.shouldRenderFace(state, world, pos, face) && !(world instanceof WrappedWorld))
			return false;
		return true;
	}

	@Override
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof GaugeTileEntity))
			return;
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		if (gaugeTE.dialTarget == 0)
			return;
		int color = gaugeTE.color;

		for (Direction face : Iterate.directions) {
			if (!shouldRenderHeadOnFace(worldIn, pos, stateIn, face))
				continue;

			Vec3 rgb = Color.vectorFromRGB(color);
			Vec3 faceVec = Vec3.atLowerCornerOf(face.getNormal());
			Direction positiveFacing = Direction.get(AxisDirection.POSITIVE, face.getAxis());
			Vec3 positiveFaceVec = Vec3.atLowerCornerOf(positiveFacing.getNormal());
			int particleCount = gaugeTE.dialTarget > 1 ? 4 : 1;

			if (particleCount == 1 && rand.nextFloat() > 1 / 4f)
				continue;

			for (int i = 0; i < particleCount; i++) {
				Vec3 mul = VecHelper.offsetRandomly(Vec3.ZERO, rand, .25f)
					.multiply(new Vec3(1, 1, 1).subtract(positiveFaceVec))
					.normalize()
					.scale(.3f);
				Vec3 offset = VecHelper.getCenterOf(pos)
					.add(faceVec.scale(.55))
					.add(mul);
				worldIn.addParticle(new DustParticleOptions((float) rgb.x, (float) rgb.y, (float) rgb.z, 1), offset.x,
					offset.y, offset.z, mul.x, mul.y, mul.z);
			}

		}

	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return GAUGE.get(state.getValue(FACING), state.getValue(AXIS_ALONG_FIRST_COORDINATE));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof GaugeTileEntity) {
			GaugeTileEntity gaugeTileEntity = (GaugeTileEntity) te;
			return Mth.ceil(Mth.clamp(gaugeTileEntity.dialTarget * 14, 0, 15));
		}
		return 0;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}
}
