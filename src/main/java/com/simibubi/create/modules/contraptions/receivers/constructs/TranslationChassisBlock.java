package com.simibubi.create.modules.contraptions.receivers.constructs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class TranslationChassisBlock extends AbstractChassisBlock {

	public static final BooleanProperty STICKY_TOP = BooleanProperty.create("sticky_top");
	public static final BooleanProperty STICKY_BOTTOM = BooleanProperty.create("sticky_bottom");

	public TranslationChassisBlock() {
		super(Properties.from(Blocks.PISTON));
		setDefaultState(getDefaultState().with(STICKY_TOP, false).with(STICKY_BOTTOM, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(STICKY_TOP, STICKY_BOTTOM);
		super.fillStateContainer(builder);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos placedOnPos = context.getPos().offset(context.getFace().getOpposite());
		BlockState blockState = context.getWorld().getBlockState(placedOnPos);
		if (blockState.getBlock() instanceof TranslationChassisBlock && !context.isPlacerSneaking())
			return getDefaultState().with(AXIS, blockState.get(AXIS));
		return super.getStateForPlacement(context);
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		return face.getAxisDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
	}

}
