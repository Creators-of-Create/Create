package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.List;

import com.simibubi.create.content.contraptions.fluids.FluidPipeAttachmentBehaviour;
import com.simibubi.create.content.contraptions.fluids.FluidPipeBehaviour;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

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

	static class StraightPipeAttachmentBehaviour extends FluidPipeAttachmentBehaviour {

		public StraightPipeAttachmentBehaviour(SmartTileEntity te) {
			super(te);
		}

		@Override
		public AttachmentTypes getAttachment(IBlockDisplayReader world, BlockPos pos, BlockState state, Direction direction) {
			AttachmentTypes attachment = super.getAttachment(world, pos, state, direction);
			BlockState otherState = world.getBlockState(pos.offset(direction));

			Axis axis = IAxisPipe.getAxisOf(state);
			Axis otherAxis = IAxisPipe.getAxisOf(otherState);

			if (axis == otherAxis && axis != null)
				if (state.getBlock() == otherState.getBlock() || direction.getAxisDirection() == AxisDirection.POSITIVE)
					return AttachmentTypes.NONE;
			
			if (otherState.getBlock() instanceof FluidValveBlock
				&& FluidValveBlock.getPipeAxis(otherState) == direction.getAxis())
				return AttachmentTypes.NONE;

			return attachment;
		}

	}

}
