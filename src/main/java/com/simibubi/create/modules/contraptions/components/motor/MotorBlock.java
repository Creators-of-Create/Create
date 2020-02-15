package com.simibubi.create.modules.contraptions.components.motor;

import com.simibubi.create.foundation.block.IHaveScrollableValue;
import com.simibubi.create.foundation.block.IWithTileEntity;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;

public class MotorBlock extends HorizontalKineticBlock
		implements IWithTileEntity<MotorTileEntity>, IHaveScrollableValue {

	private static final Vec3d valuePos = new Vec3d(15 / 16f, 5 / 16f, 5 / 16f);

	public MotorBlock() {
		super(Properties.from(Blocks.IRON_BLOCK));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.MOTOR_BLOCK.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MotorTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction preferred = getPreferredHorizontalFacing(context);
		if (context.isPlacerSneaking() || preferred == null)
			return super.getStateForPlacement(context);
		return getDefaultState().with(HORIZONTAL_FACING, preferred);
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.get(HORIZONTAL_FACING);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(HORIZONTAL_FACING).getAxis();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public int getCurrentValue(BlockState state, IWorld world, BlockPos pos) {
		MotorTileEntity tileEntity = (MotorTileEntity) world.getTileEntity(pos);
		if (tileEntity == null)
			return 0;
		return tileEntity.newGeneratedSpeed;
	}

	@Override
	public void onScroll(BlockState state, IWorld world, BlockPos pos, double delta) {
		withTileEntityDo(world, pos, te -> {
			boolean forward = delta > 0;
			int step = forward ? 1 : -1;
			int currentSpeed = te.newGeneratedSpeed;

			if (world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ()).isSneaking()){
				te.setSpeedValueLazily(currentSpeed + step);
				return;
			}

			int magnitude = Math.abs(currentSpeed) - (forward == currentSpeed > 0 ? 0 : 1);

			if (magnitude >= 4)
				step *= 4;
			if (magnitude >= 32)
				step *= 4;
			if (magnitude >= 128)
				step *= 4;

			te.setSpeedValueLazily(currentSpeed + step);
		});
	}

	@Override
	public String getValueName(BlockState state, IWorld world, BlockPos pos) {
		return Lang.translate("generic.speed") + " (" + Lang.translate("generic.unit.rpm") + ")";
	}

	@Override
	public Vec3d getValueBoxPosition(BlockState state, IWorld world, BlockPos pos) {
		return valuePos;
	}

	@Override
	public Direction getValueBoxDirection(BlockState state, IWorld world, BlockPos pos) {
		return state.get(HORIZONTAL_FACING).getOpposite();
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}
}
