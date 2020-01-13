package com.simibubi.create.modules.contraptions.components.contraptions.chassis;

import java.util.Arrays;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

public class LinearChassisBlock extends AbstractChassisBlock implements IHaveConnectedTextures {

	public static final BooleanProperty STICKY_TOP = BooleanProperty.create("sticky_top");
	public static final BooleanProperty STICKY_BOTTOM = BooleanProperty.create("sticky_bottom");

	public LinearChassisBlock() {
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

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return new ChassisCTBehaviour();
	}
	
	private static class ChassisCTBehaviour extends ConnectedTextureBehaviour {

		static final CTSpriteShiftEntry regular = CTSpriteShifter.get(CTType.OMNIDIRECTIONAL,
				"translation_chassis_top");
		static final CTSpriteShiftEntry sticky = CTSpriteShifter.get(CTType.OMNIDIRECTIONAL,
				"translation_chassis_top_sticky");

		@Override
		public CTSpriteShiftEntry get(BlockState state, Direction direction) {
			Block block = state.getBlock();
			BooleanProperty glueableSide = ((LinearChassisBlock) block).getGlueableSide(state, direction);
			if (glueableSide == null)
				return null;
			return state.get(glueableSide) ? sticky : regular;
		}
		
		@Override
		public Iterable<CTSpriteShiftEntry> getAllCTShifts() {
			return Arrays.asList(regular, sticky);
		}

		@Override
		public boolean shouldFlipUVs(BlockState state, Direction face) {
			if (state.get(AXIS).isHorizontal() && face.getAxisDirection() == AxisDirection.POSITIVE)
				return true;
			return super.shouldFlipUVs(state, face);
		}

		@Override
		public boolean connectsTo(BlockState state, BlockState other, IEnviromentBlockReader reader, BlockPos pos,
				BlockPos otherPos, Direction face) {
			return sameKind(state, other) && state.get(AXIS) == other.get(AXIS);
		}

	}

}
