package com.simibubi.create.modules.contraptions.components.waterwheel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class WaterWheelBlock extends HorizontalKineticBlock {

	public WaterWheelBlock() {
		super(Properties.from(Blocks.STRIPPED_SPRUCE_WOOD));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new WaterWheelTileEntity();
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos neighbourPos = pos.offset(direction);
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!AllBlocks.WATER_WHEEL.typeOf(neighbourState))
				continue;
			if (neighbourState.get(HORIZONTAL_FACING).getAxis() != state.get(HORIZONTAL_FACING).getAxis()
					|| state.get(HORIZONTAL_FACING).getAxis() != direction.getAxis())
				return false;
		}

		return true;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		updateFlowAt(stateIn, worldIn.getWorld(), currentPos, facing);
		updateWheelSpeed(worldIn, currentPos);
		return stateIn;
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		for (Direction d : Direction.values())
			updateFlowAt(state, worldIn, pos, d);
		updateWheelSpeed(worldIn, pos);
	}

	private void updateFlowAt(BlockState state, World world, BlockPos pos, Direction f) {
		WaterWheelTileEntity te = (WaterWheelTileEntity) world.getTileEntity(pos);
		if (te == null)
			return;
		if (f.getAxis() == state.get(HORIZONTAL_FACING).getAxis())
			return;
		IFluidState fluid = world.getFluidState(pos.offset(f));
		Vec3d flowVec = fluid.getFlow(world, pos.offset(f));
		Direction wf = state.get(HORIZONTAL_FACING);
		double flow = 0;

		flowVec = flowVec.scale(f.getAxisDirection().getOffset());
		boolean clockwise = wf.getAxisDirection() == AxisDirection.POSITIVE;
		int clockwiseMultiplier = 2; 

		if (wf.getAxis() == Axis.Z) {
			if (f.getAxis() == Axis.Y)
				flow = flowVec.x > 0 ^ !clockwise ? -flowVec.x * clockwiseMultiplier : -flowVec.x;
			if (f.getAxis() == Axis.X)
				flow = flowVec.y < 0 ^ !clockwise ? flowVec.y * clockwiseMultiplier : flowVec.y;
		}

		if (wf.getAxis() == Axis.X) {
			if (f.getAxis() == Axis.Y)
				flow = flowVec.z < 0 ^ !clockwise ? flowVec.z * clockwiseMultiplier : flowVec.z;
			if (f.getAxis() == Axis.Z)
				flow = flowVec.y > 0 ^ !clockwise ? -flowVec.y * clockwiseMultiplier : -flowVec.y;
		}

		te.setFlow(f, (int) (flow * 5));
	}

	private void updateWheelSpeed(IWorld world, BlockPos pos) {
		if (world.isRemote())
			return;
		WaterWheelTileEntity te = (WaterWheelTileEntity) world.getTileEntity(pos);
		if (te == null)
			return;
		te.updateGeneratedRotation();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = context.getFace();
		BlockState placedOn = context.getWorld().getBlockState(context.getPos().offset(facing.getOpposite()));
		if (AllBlocks.WATER_WHEEL.typeOf(placedOn))
			return getDefaultState().with(HORIZONTAL_FACING, placedOn.get(HORIZONTAL_FACING));
		if (facing.getAxis().isHorizontal())
			return getDefaultState().with(HORIZONTAL_FACING,
					context.isPlacerSneaking() ? facing.getOpposite() : facing);

		return super.getStateForPlacement(context);
	}

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return state.get(HORIZONTAL_FACING).getAxis() == face.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis();
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
	
}
