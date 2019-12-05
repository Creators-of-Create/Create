package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.modules.contraptions.base.RotatedPillarKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ShaftBlock extends RotatedPillarKineticBlock {

	public static final VoxelShape AXIS_X = makeCuboidShape(0, 5, 5, 16, 11, 11);
	public static final VoxelShape AXIS_Y = makeCuboidShape(5, 0, 5, 11, 16, 11);
	public static final VoxelShape AXIS_Z = makeCuboidShape(5, 5, 0, 11, 11, 16);

	public ShaftBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ShaftTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return state.get(AXIS) == Axis.X ? AXIS_X : state.get(AXIS) == Axis.Z ? AXIS_Z : AXIS_Y;
	}

	@Override
	public float getParticleTargetRadius() {
		return .25f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 0f;
	}

	// IRotate:

	@Override
	public boolean hasShaftTowards(World world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.get(AXIS);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.get(AXIS);
	}

}
