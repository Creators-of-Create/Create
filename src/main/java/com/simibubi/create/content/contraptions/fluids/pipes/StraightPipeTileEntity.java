package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StraightPipeTileEntity extends SmartTileEntity {

	public StraightPipeTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StraightPipeFluidTransportBehaviour(this));
		behaviours.add(new BracketedTileEntityBehaviour(this));
		registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
	}

	static class StraightPipeFluidTransportBehaviour extends FluidTransportBehaviour {

		public StraightPipeFluidTransportBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canHaveFlowToward(BlockState state, Direction direction) {
			return state.hasProperty(AxisPipeBlock.AXIS) && state.getValue(AxisPipeBlock.AXIS) == direction.getAxis();
		}

		@Override
		public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
			Direction direction) {
			AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
			BlockState otherState = world.getBlockState(pos.relative(direction));

			Axis axis = IAxisPipe.getAxisOf(state);
			Axis otherAxis = IAxisPipe.getAxisOf(otherState);

			if (attachment == AttachmentTypes.RIM && state.getBlock() instanceof FluidValveBlock)
				return AttachmentTypes.NONE;
			if (attachment == AttachmentTypes.RIM && FluidPipeBlock.isPipe(otherState))
				return AttachmentTypes.PARTIAL_RIM;
			if (axis == otherAxis && axis != null)
				return AttachmentTypes.NONE;

			if (otherState.getBlock() instanceof FluidValveBlock
				&& FluidValveBlock.getPipeAxis(otherState) == direction.getAxis())
				return AttachmentTypes.NONE;

			return attachment.withoutConnector();
		}

	}

}
