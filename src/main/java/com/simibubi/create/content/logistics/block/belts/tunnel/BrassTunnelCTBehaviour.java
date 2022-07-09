package com.simibubi.create.content.logistics.block.belts.tunnel;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BrassTunnelCTBehaviour extends ConnectedTextureBehaviour.Base {

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		return direction == Direction.UP ? AllSpriteShifts.BRASS_TUNNEL_TOP : null;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		int yDiff = otherPos.getY() - pos.getY();
		int zDiff = otherPos.getZ() - pos.getZ();
		if (yDiff != 0)
			return false;

		BlockEntity te = reader.getBlockEntity(pos);
		if (!(te instanceof BrassTunnelTileEntity))
			return false;
		BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) te;
		boolean leftSide = zDiff > 0;
		return tunnelTE.isConnected(leftSide);
	}

}
