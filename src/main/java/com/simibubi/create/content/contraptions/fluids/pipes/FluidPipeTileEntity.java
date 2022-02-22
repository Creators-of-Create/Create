package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.components.structureMovement.ITransformableTE;
import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FluidPipeTileEntity extends SmartTileEntity implements ITransformableTE {

	public FluidPipeTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StandardPipeFluidTransportBehaviour(this));
		behaviours.add(new BracketedTileEntityBehaviour(this, this::canHaveBracket)
			.withTrigger(state -> AllTriggers.BRACKET_APPLY_TRIGGER.constructTriggerFor(state.getBlock())));
	}

	@Override
	public void transform(StructureTransform transform) {
		BracketedTileEntityBehaviour bracketBehaviour = getBehaviour(BracketedTileEntityBehaviour.TYPE);
		if (bracketBehaviour != null) {
			bracketBehaviour.transformBracket(transform);
		}
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
		public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
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
