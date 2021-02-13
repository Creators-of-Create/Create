package com.simibubi.create.content.contraptions.relays.elementary;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;

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
	public AxisAlignedBB makeRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}

}
