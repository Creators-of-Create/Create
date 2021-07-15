package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.Random;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class GaugeBlock extends DirectionalAxisKineticBlock {

	public static final GaugeShaper GAUGE = GaugeShaper.make();
	protected Type type;

	public enum Type implements IStringSerializable {
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
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		World world = context.getLevel();
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
	protected Direction getFacingForPlacement(BlockItemUseContext context) {
		return context.getClickedFace();
	}

	@Override
	protected boolean getAxisAlignmentForPlacement(BlockItemUseContext context) {
		return context.getHorizontalDirection()
			.getAxis() != Axis.X;
	}

	public boolean shouldRenderHeadOnFace(World world, BlockPos pos, BlockState state, Direction face) {
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
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te == null || !(te instanceof GaugeTileEntity))
			return;
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		if (gaugeTE.dialTarget == 0)
			return;
		int color = gaugeTE.color;

		for (Direction face : Iterate.directions) {
			if (!shouldRenderHeadOnFace(worldIn, pos, stateIn, face))
				continue;

			Vector3d rgb = ColorHelper.getRGB(color);
			Vector3d faceVec = Vector3d.atLowerCornerOf(face.getNormal());
			Direction positiveFacing = Direction.get(AxisDirection.POSITIVE, face.getAxis());
			Vector3d positiveFaceVec = Vector3d.atLowerCornerOf(positiveFacing.getNormal());
			int particleCount = gaugeTE.dialTarget > 1 ? 4 : 1;

			if (particleCount == 1 && rand.nextFloat() > 1 / 4f)
				continue;

			for (int i = 0; i < particleCount; i++) {
				Vector3d mul = VecHelper.offsetRandomly(Vector3d.ZERO, rand, .25f)
					.multiply(new Vector3d(1, 1, 1).subtract(positiveFaceVec))
					.normalize()
					.scale(.3f);
				Vector3d offset = VecHelper.getCenterOf(pos)
					.add(faceVec.scale(.55))
					.add(mul);
				worldIn.addParticle(new RedstoneParticleData((float) rgb.x, (float) rgb.y, (float) rgb.z, 1), offset.x,
					offset.y, offset.z, mul.x, mul.y, mul.z);
			}

		}

	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return GAUGE.get(state.getValue(FACING), state.getValue(AXIS_ALONG_FIRST_COORDINATE));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof GaugeTileEntity) {
			GaugeTileEntity gaugeTileEntity = (GaugeTileEntity) te;
			return MathHelper.ceil(MathHelper.clamp(gaugeTileEntity.dialTarget * 14, 0, 15));
		}
		return 0;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}
}
