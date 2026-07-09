package com.simibubi.create.foundation.render;

import java.util.function.BiFunction;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

/**
 * Temporary Create 26.2 bridge for legacy render layer callers.
 * TODO 26.2: Rebuild Create's custom pipelines with RenderPipeline/RenderSetup.
 */
@Deprecated(forRemoval = true)
public class RenderTypes {
	public static final BiFunction<Identifier, Boolean, RenderType> TRAIN_MAP =
		(texture, linearFiltering) -> net.minecraft.client.renderer.rendertype.RenderTypes.text(texture);

	public static RenderType entitySolidBlockMipped() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType entityCutoutBlockMipped() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType entityTranslucentBlockMipped() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType additive() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType itemGlowingSolid() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType itemGlowingTranslucent() {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucentEmissive(TextureAtlas.LOCATION_BLOCKS);
	}

	public static RenderType chain(Identifier location) {
		return net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(location, false);
	}
}
