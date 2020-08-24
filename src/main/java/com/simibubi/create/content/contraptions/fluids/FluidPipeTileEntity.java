package com.simibubi.create.content.contraptions.fluids;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public class FluidPipeTileEntity extends SmartTileEntity {

	FluidPipeBehaviour behaviour;

	public FluidPipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviour = new StandardPipeBehaviour(this);
		behaviours.add(behaviour);
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

}
