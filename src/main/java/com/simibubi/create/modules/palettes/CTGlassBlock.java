package com.simibubi.create.modules.palettes;

import java.util.function.Supplier;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.IHaveConnectedTextures;

import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;

public class CTGlassBlock extends GlassBlock implements IHaveConnectedTextures {

	private Supplier<ResourceLocation> textureToReplace;
	private boolean hasAlpha;

	public CTGlassBlock(boolean hasAlpha) {
		super(Properties.from(Blocks.GLASS));
		textureToReplace = () -> {
			return new ResourceLocation(Create.ID, "block/" + getRegistryName().getPath());
		};
		this.hasAlpha = hasAlpha;
	}

	@Override
	public boolean appliesTo(BakedQuad quad) {
		return quad.getSprite().getName().equals(textureToReplace.get());
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return hasAlpha ? BlockRenderLayer.TRANSLUCENT : super.getRenderLayer();
	}

}
