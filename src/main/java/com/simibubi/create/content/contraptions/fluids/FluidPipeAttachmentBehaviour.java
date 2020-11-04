package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class FluidPipeAttachmentBehaviour extends BracketedTileEntityBehaviour {

	public static BehaviourType<FluidPipeAttachmentBehaviour> TYPE = new BehaviourType<>();

	public AttachmentTypes getAttachment(ILightReader world, BlockPos pos, BlockState state, Direction direction) {
		if (!isPipeConnectedTowards(state, direction))
			return AttachmentTypes.NONE;

		BlockPos offsetPos = pos.offset(direction);
		BlockState facingState = world.getBlockState(offsetPos);

		if (facingState.getBlock() instanceof PumpBlock && facingState.get(PumpBlock.FACING)
			.getAxis() == direction.getAxis())
			return AttachmentTypes.NONE;

		if (FluidPropagator.hasFluidCapability(facingState, world, offsetPos, direction)
			&& !AllBlocks.HOSE_PULLEY.has(facingState))
			return AttachmentTypes.DRAIN;

		return AttachmentTypes.RIM;
	}

	public boolean isPipeConnectedTowards(BlockState state, Direction direction) {
		FluidPipeBehaviour fluidPipeBehaviour = tileEntity.getBehaviour(FluidPipeBehaviour.TYPE);
		if (fluidPipeBehaviour == null)
			return false;
//		BlockState bracket = getBracket();
//		if (bracket != Blocks.AIR.getDefaultState() && bracket.get(BracketBlock.FACING) == direction)
//			return false;
		return fluidPipeBehaviour.isConnectedTo(state, direction);
	}

	public static enum AttachmentTypes {
		NONE, RIM, DRAIN;

		public boolean hasModel() {
			return this != NONE;
		}
	}

	public FluidPipeAttachmentBehaviour(SmartTileEntity te) {
		super(te);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean canHaveBracket() {
		BlockState blockState = tileEntity.getBlockState();
		if (blockState.getBlock() instanceof PumpBlock)
			return false;
		if (blockState.getBlock() instanceof EncasedPipeBlock)
			return false;
		return true;
	}

}
