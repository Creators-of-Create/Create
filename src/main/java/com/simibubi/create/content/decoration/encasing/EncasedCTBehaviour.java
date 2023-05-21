package com.simibubi.create.content.decoration.encasing;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedCTBehaviour extends ConnectedTextureBehaviour.Base {

	private CTSpriteShiftEntry shift;

	public EncasedCTBehaviour(CTSpriteShiftEntry shift) {
		this.shift = shift;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
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
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		return shift;
	}

}
