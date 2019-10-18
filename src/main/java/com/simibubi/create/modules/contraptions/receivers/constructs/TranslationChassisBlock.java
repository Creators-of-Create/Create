package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllBlocks;

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
		if (isChassis(blockState) && !context.isPlacerSneaking())
			return getDefaultState().with(AXIS, blockState.get(AXIS));
		if (!context.isPlacerSneaking())
			return getDefaultState().with(AXIS, context.getNearestLookingDirection().getAxis());
		return super.getStateForPlacement(context);
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		if (face.getAxis() != state.get(AXIS))
			return null;
		return face.getAxisDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
	}
	
	@Override
	public String getTranslationKey() {
		Block block = AllBlocks.TRANSLATION_CHASSIS.get();
		if (this == block)
			return super.getTranslationKey();
		return block.getTranslationKey();
	}

	public static boolean isChassis(BlockState state) {
		return AllBlocks.TRANSLATION_CHASSIS.typeOf(state) || AllBlocks.TRANSLATION_CHASSIS_SECONDARY.typeOf(state);
	}

	public static boolean sameKind(BlockState state1, BlockState state2) {
		return state1.getBlock() == state2.getBlock();
	}

}
