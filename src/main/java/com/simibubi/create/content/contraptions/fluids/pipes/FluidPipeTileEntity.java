package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.FluidPipeAttachmentBehaviour;
import com.simibubi.create.content.contraptions.fluids.FluidPipeBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class FluidPipeTileEntity extends SmartTileEntity {

	public FluidPipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StandardPipeBehaviour(this));
		behaviours.add(new StandardPipeAttachmentBehaviour(this));
	}

	class StandardPipeBehaviour extends FluidPipeBehaviour {

		public StandardPipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean isConnectedTo(BlockState state, Direction direction) {
			return FluidPipeBlock.isPipe(state) && state.get(FluidPipeBlock.FACING_TO_PROPERTY_MAP.get(direction));
		}

	}

	class StandardPipeAttachmentBehaviour extends FluidPipeAttachmentBehaviour {

		public StandardPipeAttachmentBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public AttachmentTypes getAttachment(ILightReader world, BlockPos pos, BlockState state, Direction direction) {
			AttachmentTypes attachment = super.getAttachment(world, pos, state, direction);

			BlockPos offsetPos = pos.offset(direction);
			if (!FluidPipeBlock.isPipe(world.getBlockState(offsetPos))) {
				FluidPipeAttachmentBehaviour attachmentBehaviour =
					TileEntityBehaviour.get(world, offsetPos, FluidPipeAttachmentBehaviour.TYPE);
				if (attachmentBehaviour != null && attachmentBehaviour
					.isPipeConnectedTowards(world.getBlockState(offsetPos), direction.getOpposite()))
					return AttachmentTypes.NONE;
			}

			if (attachment == AttachmentTypes.RIM && !FluidPipeBlock.shouldDrawRim(world, pos, state, direction))
				return AttachmentTypes.NONE;
			return attachment;
		}

	}

}
