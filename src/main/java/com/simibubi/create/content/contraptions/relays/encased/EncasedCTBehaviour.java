package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class EncasedCTBehaviour extends ConnectedTextureBehaviour {

	private CTSpriteShiftEntry shift;

	public EncasedCTBehaviour(CTSpriteShiftEntry shift) {
		this.shift = shift;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, IBlockDisplayReader reader, BlockPos pos, BlockPos otherPos,
							  Direction face) {
		if (isBeingBlocked(state, reader, pos, otherPos, face))
			return false;
		CasingConnectivity cc = CreateClient.CASING_CONNECTIVITY;
		CasingConnectivity.Entry entry = cc.get(state);
		CasingConnectivity.Entry otherEntry = cc.get(other);
		if (entry == null || otherEntry == null)
			return false;
		if (!entry.isSideValid(state, face) || !otherEntry.isSideValid(other, face))
			return false;
		if (entry.getCasing() != otherEntry.getCasing())
			return false;
		return true;
	}

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		return shift;
	}

}
