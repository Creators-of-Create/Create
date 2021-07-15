package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour.CTContext;

public class BrassTunnelCTBehaviour extends ConnectedTextureBehaviour {

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		return direction == Direction.UP ? AllSpriteShifts.BRASS_TUNNEL_TOP : null;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, IBlockDisplayReader reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		int yDiff = otherPos.getY() - pos.getY();
		int zDiff = otherPos.getZ() - pos.getZ();
		if (yDiff != 0)
			return false;

		TileEntity te = reader.getBlockEntity(pos);
		if (!(te instanceof BrassTunnelTileEntity))
			return false;
		BrassTunnelTileEntity tunnelTE = (BrassTunnelTileEntity) te;
		boolean leftSide = zDiff > 0;
		return tunnelTE.isConnected(leftSide);
	}

	@Override
	public CTContext buildContext(IBlockDisplayReader reader, BlockPos pos, BlockState state, Direction face) {
		return super.buildContext(reader, pos, state, face);
	}

	@Override
	protected boolean reverseUVs(BlockState state, Direction face) {
		return super.reverseUVs(state, face);
	}

	@Override
	protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
		return super.reverseUVsHorizontally(state, face);
	}

	@Override
	protected boolean reverseUVsVertically(BlockState state, Direction face) {
		return super.reverseUVsVertically(state, face);
	}

}
