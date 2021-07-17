package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class FluidPipeTileEntity extends SmartTileEntity {

	public FluidPipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StandardPipeFluidTransportBehaviour(this));
		behaviours.add(new BracketedTileEntityBehaviour(this, this::canHaveBracket)
			.withTrigger(state -> AllTriggers.BRACKET_APPLY_TRIGGER.constructTriggerFor(state.getBlock())));
	}

	private boolean canHaveBracket(BlockState state) {
		return !(state.getBlock() instanceof EncasedPipeBlock);
	}

	class StandardPipeFluidTransportBehaviour extends FluidTransportBehaviour {

		public StandardPipeFluidTransportBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean canHaveFlowToward(BlockState state, Direction direction) {
			return (FluidPipeBlock.isPipe(state) || state.getBlock() instanceof EncasedPipeBlock)
				&& state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
		}

		@Override
		public AttachmentTypes getRenderedRimAttachment(IBlockDisplayReader world, BlockPos pos, BlockState state,
			Direction direction) {
			AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

			if (attachment == AttachmentTypes.RIM && AllBlocks.ENCASED_FLUID_PIPE.has(state))
				return AttachmentTypes.RIM;

			BlockPos offsetPos = pos.relative(direction);
			if (!FluidPipeBlock.isPipe(world.getBlockState(offsetPos))) {
				FluidTransportBehaviour pipeBehaviour =
					TileEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
				if (pipeBehaviour != null
					&& pipeBehaviour.canHaveFlowToward(world.getBlockState(offsetPos), direction.getOpposite()))
					return AttachmentTypes.NONE;
			}

			if (attachment == AttachmentTypes.RIM && !FluidPipeBlock.shouldDrawRim(world, pos, state, direction))
				return AttachmentTypes.NONE;
			return attachment;
		}

	}

}
