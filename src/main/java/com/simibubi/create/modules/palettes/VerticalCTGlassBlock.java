package com.simibubi.create.modules.palettes;

import com.simibubi.create.AllCTs;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class VerticalCTGlassBlock extends CTGlassBlock {

	public VerticalCTGlassBlock(AllCTs spriteShift, boolean hasAlpha) {
		super(spriteShift, hasAlpha);
	}

	@Override
	public ConnectedTextureBehaviour createBehaviour(CTSpriteShiftEntry spriteShift) {
		return new StandardCTBehaviour(spriteShift) {
			@Override
			public CTSpriteShiftEntry get(BlockState state, Direction direction) {
				if (direction.getAxis().isVertical())
					return null;
				return super.get(state, direction);
			}
		};
	}

}
