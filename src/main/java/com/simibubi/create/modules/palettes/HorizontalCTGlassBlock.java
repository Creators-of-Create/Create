package com.simibubi.create.modules.palettes;

import javax.annotation.Nullable;

import com.simibubi.create.AllCTs;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

public class HorizontalCTGlassBlock extends CTGlassBlock {

	private AllCTs topShift;

	public HorizontalCTGlassBlock(AllCTs layerShift, @Nullable AllCTs topShift, boolean hasAlpha) {
		super(layerShift, hasAlpha);
		this.topShift = topShift;
		behaviour = createBehaviour(layerShift.get());
	}

	@Override
	public ConnectedTextureBehaviour createBehaviour(CTSpriteShiftEntry spriteShift) {
		return new LayeredCTBlock.LayeredCTBehaviour(spriteShift, topShift == null ? null : topShift.get());
	}
	
}
