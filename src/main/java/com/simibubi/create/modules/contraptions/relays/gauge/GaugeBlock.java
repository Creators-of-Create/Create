package com.simibubi.create.modules.contraptions.relays.gauge;

import java.util.Random;

import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.base.DirectionalAxisKineticBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class GaugeBlock extends DirectionalAxisKineticBlock {

	public static final GaugeShaper GAUGE = GaugeShaper.make();
	protected Type type;

	public enum Type implements IStringSerializable {
		SPEED, STRESS;

		@Override
		public String getName() {
			return Lang.asId(name());
		}
	}

	public GaugeBlock(Type type) {
		super(Properties.from(Blocks.PISTON));
		this.type = type;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		switch (type) {
		case SPEED:
			return new SpeedGaugeTileEntity();
		case STRESS:
			return new StressGaugeTileEntity();
		default:
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return Blocks.SPRUCE_PLANKS.getMaterialColor(state, worldIn, pos);
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		Direction facing = state.get(FACING).getOpposite();
		BlockPos neighbourPos = pos.offset(facing);
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return Block.hasSolidSide(neighbour, worldIn, neighbourPos, facing.getOpposite());
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
		if (worldIn.isRemote)
			return;

		Direction blockFacing = state.get(FACING);
		if (fromPos.equals(pos.offset(blockFacing.getOpposite()))) {
			if (!isValidPosition(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
	}

	@Override
	protected Direction getFacingForPlacement(BlockItemUseContext context) {
		return context.getFace();
	}

	protected boolean getAxisAlignmentForPlacement(BlockItemUseContext context) {
		return context.getPlacementHorizontalFacing().getAxis() != Axis.X;
	}

	public boolean shouldRenderHeadOnFace(World world, BlockPos pos, BlockState state, Direction face) {
		if (face.getAxis().isVertical())
			return false;
		if (face == state.get(FACING).getOpposite())
			return false;
		if (face.getAxis() == getRotationAxis(state))
			return false;
		if (getRotationAxis(state) == Axis.Y && face != state.get(FACING))
			return false;
		BlockState blockState = world.getBlockState(pos.offset(face));
		if (Block.hasSolidSide(blockState, world, pos, face.getOpposite())
				&& blockState.getMaterial() != Material.GLASS)
			return false;
		return true;
	}

	@Override
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null || !(te instanceof GaugeTileEntity))
			return;
		GaugeTileEntity gaugeTE = (GaugeTileEntity) te;
		if (gaugeTE.dialTarget == 0)
			return;
		int color = gaugeTE.color;

		for (Direction face : Direction.values()) {
			if (!shouldRenderHeadOnFace(worldIn, pos, stateIn, face))
				continue;

			Vec3d rgb = ColorHelper.getRGB(color);
			Vec3d faceVec = new Vec3d(face.getDirectionVec());
			Direction positiveFacing = Direction.getFacingFromAxis(AxisDirection.POSITIVE, face.getAxis());
			Vec3d positiveFaceVec = new Vec3d(positiveFacing.getDirectionVec());
			int particleCount = gaugeTE.dialTarget > 1 ? 4 : 1;

			if (particleCount == 1 && rand.nextFloat() > 1 / 4f)
				continue;

			for (int i = 0; i < particleCount; i++) {
				Vec3d mul = VecHelper.offsetRandomly(Vec3d.ZERO, rand, .25f)
						.mul(new Vec3d(1, 1, 1).subtract(positiveFaceVec)).normalize().scale(.3f);
				Vec3d offset = VecHelper.getCenterOf(pos).add(faceVec.scale(.55)).add(mul);
				worldIn.addParticle(new RedstoneParticleData((float) rgb.x, (float) rgb.y, (float) rgb.z, 1), offset.x,
						offset.y, offset.z, mul.x, mul.y, mul.z);
			}

		}

	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return GAUGE.get(state.get(FACING), state.get(AXIS_ALONG_FIRST_COORDINATE));
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof GaugeTileEntity) {
			GaugeTileEntity gaugeTileEntity = (GaugeTileEntity) te;
			return MathHelper.ceil(MathHelper.clamp(gaugeTileEntity.dialTarget * 14, 0, 15));
		}
		return 0;
	}

}
