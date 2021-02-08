package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class SimpleKineticTileEntity extends KineticTileEntity {

	public SimpleKineticTileEntity(TileEntityType<? extends SimpleKineticTileEntity> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		behaviours.add(
			new BracketedTileEntityBehaviour(this, state -> state.getBlock() instanceof AbstractShaftBlock).withTrigger(
				state -> state.getBlock() instanceof ShaftBlock ? AllTriggers.BRACKET_SHAFT : AllTriggers.BRACKET_COG));
		super.addBehaviours(behaviours);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}

	@Override
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		if (!AllBlocks.LARGE_COGWHEEL.has(state))
			return super.addPropagationLocations(block, state, neighbours);

		BlockPos.getAllInBox(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
			.forEach(offset -> {
				if (offset.distanceSq(0, 0, 0, false) == BlockPos.ZERO.distanceSq(1, 1, 0, false))
					neighbours.add(pos.add(offset));
			});
		return neighbours;
	}

}
