package com.simibubi.create.foundation.render;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public class LegacyRenderTypes {

	public static RenderType solid() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType cutout() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType cutoutMipped() {
		return cutout();
	}

	public static RenderType translucent() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType lines() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.lines();
	}

	public static RenderType shadow(Identifier texture) {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityShadow(texture);
	}
}
