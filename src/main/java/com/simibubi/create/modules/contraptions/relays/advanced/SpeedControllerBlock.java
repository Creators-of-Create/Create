package com.simibubi.create.modules.contraptions.relays.advanced;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.HorizontalAxisKineticBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class SpeedControllerBlock extends HorizontalAxisKineticBlock {

	public SpeedControllerBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SpeedControllerTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState above = context.getWorld().getBlockState(context.getPos().up());
		if (AllBlocks.LARGE_COGWHEEL.typeOf(above) && above.get(CogWheelBlock.AXIS).isHorizontal())
			return getDefaultState().with(HORIZONTAL_AXIS, above.get(CogWheelBlock.AXIS) == Axis.X ? Axis.Z : Axis.X);
		return super.getStateForPlacement(context);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.SPEED_CONTROLLER.get(state.get(HORIZONTAL_AXIS));
	}

}
