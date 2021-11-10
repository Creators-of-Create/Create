package com.simibubi.create.content.logistics.block.vault;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class VaultCTBehaviour extends ConnectedTextureBehaviour {

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		Axis vaultBlockAxis = VaultBlock.getVaultBlockAxis(state);
		boolean small = !VaultBlock.isLarge(state);
		if (vaultBlockAxis == null)
			return null;

		if (direction.getAxis() == vaultBlockAxis)
			return AllSpriteShifts.VAULT_FRONT.get(small);
		if (direction == Direction.UP)
			return AllSpriteShifts.VAULT_TOP.get(small);
		if (direction == Direction.DOWN)
			return AllSpriteShifts.VAULT_BOTTOM.get(small);

		return AllSpriteShifts.VAULT_SIDE.get(small);
	}

	@Override
	protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Axis vaultBlockAxis = VaultBlock.getVaultBlockAxis(state);
		boolean alongX = vaultBlockAxis == Axis.X;
		if (face.getAxis()
			.isVertical() && alongX)
			return super.getUpDirection(reader, pos, state, face).getClockWise();
		if (face.getAxis() == vaultBlockAxis || face.getAxis()
			.isVertical())
			return super.getUpDirection(reader, pos, state, face);
		return Direction.fromAxisAndDirection(vaultBlockAxis, alongX ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
	}

	@Override
	protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Axis vaultBlockAxis = VaultBlock.getVaultBlockAxis(state);
		if (face.getAxis()
			.isVertical() && vaultBlockAxis == Axis.X)
			return super.getRightDirection(reader, pos, state, face).getClockWise();
		if (face.getAxis() == vaultBlockAxis || face.getAxis()
			.isVertical())
			return super.getRightDirection(reader, pos, state, face);
		return Direction.fromAxisAndDirection(Axis.Y, face.getAxisDirection());
	}

	public boolean buildContextForOccludedDirections() {
		return super.buildContextForOccludedDirections();
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		return state == other && VaultConnectivityHandler.isConnected(reader, pos, otherPos);
	}

}
