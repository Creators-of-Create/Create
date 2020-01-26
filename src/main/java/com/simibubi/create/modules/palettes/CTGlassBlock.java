package com.simibubi.create.modules.palettes;

import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import com.simibubi.create.foundation.block.connected.IHaveConnectedTextures;
import com.simibubi.create.foundation.block.connected.StandardCTBehaviour;

import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.util.BlockRenderLayer;

public class CTGlassBlock extends GlassBlock implements IHaveConnectedTextures {

	private boolean hasAlpha;

	public CTGlassBlock(boolean hasAlpha) {
		super(Properties.from(Blocks.GLASS));
		this.hasAlpha = hasAlpha;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return hasAlpha ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
	}

	@Override
	public ConnectedTextureBehaviour getBehaviour() {
		return new StandardCTBehaviour(CTSpriteShifter.get(CTType.OMNIDIRECTIONAL, getRegistryName().getPath()));
	}

}
