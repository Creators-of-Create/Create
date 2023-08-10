package com.simibubi.create.content.decoration;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MetalScaffoldingCTBehaviour extends HorizontalCTBehaviour {

	protected CTSpriteShiftEntry insideShift;

	public MetalScaffoldingCTBehaviour(CTSpriteShiftEntry outsideShift, CTSpriteShiftEntry insideShift,
		CTSpriteShiftEntry topShift) {
		super(outsideShift, topShift);
		this.insideShift = insideShift;
	}

	@Override
	public boolean buildContextForOccludedDirections() {
		return true;
	}

	@Override
	protected boolean isBeingBlocked(BlockState state, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		return face.getAxis() == Axis.Y && super.isBeingBlocked(state, reader, pos, otherPos, face);
	}

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		if (direction.getAxis() != Axis.Y && sprite == insideShift.getOriginal())
			return insideShift;
		return super.getShift(state, direction, sprite);
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		return super.connectsTo(state, other, reader, pos, otherPos, face)
			&& state.getValue(MetalScaffoldingBlock.BOTTOM) && other.getValue(MetalScaffoldingBlock.BOTTOM);
	}

}
