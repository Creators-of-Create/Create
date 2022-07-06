package com.simibubi.create.foundation.block.connected;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class HorizontalCTBehaviour extends ConnectedTextureBehaviour.Base {

	protected CTSpriteShiftEntry topShift;
	protected CTSpriteShiftEntry layerShift;

	public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift) {
		this(layerShift, null);
	}

	public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		this.layerShift = layerShift;
		this.topShift = topShift;
	}

	@Override
	public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
		return direction.getAxis()
			.isHorizontal() ? layerShift : topShift;
	}

}