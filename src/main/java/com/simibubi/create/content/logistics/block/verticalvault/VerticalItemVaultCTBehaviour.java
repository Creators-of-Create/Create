package com.simibubi.create.content.logistics.block.verticalvault;

import com.mojang.logging.LogUtils;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class VerticalItemVaultCTBehaviour extends ConnectedTextureBehaviour.Base {

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		Direction.Axis vaultBlockAxis = VerticalItemVaultBlock.getVaultBlockAxis(state);
		boolean small = !VerticalItemVaultBlock.isLarge(state);
		if (vaultBlockAxis == null)
			return null;

		if (direction == Direction.UP || direction == Direction.DOWN)
			return AllSpriteShifts.VERTICAL_VAULT_TOP.get(small);

		return AllSpriteShifts.VERTICAL_VAULT_SIDE.get(small);
	}

	@Override
	protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Direction.Axis verticalVaultBlockAxis = VerticalItemVaultBlock.getVaultBlockAxis(state);
		if (face.getAxis() == verticalVaultBlockAxis || face.getAxis()
				.isVertical()) {
			return super.getUpDirection(reader, pos, state, face);
		}
		return Direction.fromAxisAndDirection(verticalVaultBlockAxis, Direction.AxisDirection.NEGATIVE);
	}

	@Override
	protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
		Direction.Axis verticalVaultBlockAxis = VerticalItemVaultBlock.getVaultBlockAxis(state);
		if (face.getAxis() == verticalVaultBlockAxis || face.getAxis()
				.isVertical())
			return super.getRightDirection(reader, pos, state, face);
		return Direction.EAST;
	}

	public boolean buildContextForOccludedDirections() {
		return super.buildContextForOccludedDirections();
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		return state == other && ConnectivityHandler.isConnected(reader, pos, otherPos); //ItemVaultConnectivityHandler.isConnected(reader, pos, otherPos);
	}

}
