package com.simibubi.create.modules.contraptions.generators;

import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.modules.contraptions.base.HorizontalKineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MotorBlock extends HorizontalKineticBlock {

	protected static final VoxelShape MOTOR_X = makeCuboidShape(0, 3, 3, 16, 13, 13);
	protected static final VoxelShape MOTOR_Z = makeCuboidShape(3, 3, 0, 13, 13, 16);

	public MotorBlock() {
		super(Properties.create(Material.IRON));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return state.get(HORIZONTAL_FACING).getAxis() == Axis.X ? MOTOR_X : MOTOR_Z;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MotorTileEntity();
	}

	// IRotate:

	@Override
	public boolean isAxisTowards(World world, BlockPos pos, BlockState state, Direction face) {
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

	// IToolTip

	@Override
	public ItemDescription getDescription() {
		return new ItemDescription(color).withSummary("Provides Rotational Power.").withControl("When R-Clicked",
				"Opens the " + h("Configuration Screen", color)).createTabs();

	}
}
