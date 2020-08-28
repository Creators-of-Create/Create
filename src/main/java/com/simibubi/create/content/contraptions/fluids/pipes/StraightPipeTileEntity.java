package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.FluidPipeAttachmentBehaviour;
import com.simibubi.create.content.contraptions.fluids.FluidPipeBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class StraightPipeTileEntity extends SmartTileEntity {

	public StraightPipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(new StraightPipeBehaviour(this));
		behaviours.add(new StraightPipeAttachmentBehaviour(this));
	}

	class StraightPipeBehaviour extends FluidPipeBehaviour {

		public StraightPipeBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public boolean isConnectedTo(BlockState state, Direction direction) {
			return state.get(AxisPipeBlock.AXIS) == direction.getAxis();
		}

	}

	class StraightPipeAttachmentBehaviour extends FluidPipeAttachmentBehaviour {

		public StraightPipeAttachmentBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public AttachmentTypes getAttachment(ILightReader world, BlockPos pos, BlockState state, Direction direction) {
			AttachmentTypes attachment = super.getAttachment(world, pos, state, direction);
			BlockState otherState = world.getBlockState(pos.offset(direction));
			if (state.getBlock() instanceof AxisPipeBlock && otherState.getBlock() instanceof AxisPipeBlock) {
				if (state.get(AxisPipeBlock.AXIS) == otherState.get(AxisPipeBlock.AXIS)) {
					if (state.getBlock() == otherState.getBlock()
						|| direction.getAxisDirection() == AxisDirection.POSITIVE)
						return AttachmentTypes.NONE;
				}
			}
			return attachment;
		}

	}

}
