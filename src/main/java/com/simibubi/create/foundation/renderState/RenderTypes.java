package com.simibubi.create.foundation.renderState;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllSpecialTextures;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderState {

	public static RenderType getOutlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType getOutlineTranslucent(ResourceLocation texture, boolean cull) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.texture(new RenderState.TextureState(texture, false, false))
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
			.alpha(ONE_TENTH_ALPHA)
			.cull(cull ? ENABLE_CULLING : DISABLE_CULLING)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true);
		return RenderType.of("outline_translucent" + (cull ? "_cull" : ""),
			DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, rendertype$state);
	}

	private static final RenderType OUTLINE_SOLID =
		RenderType.of("outline_solid", DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true,
			false, RenderType.State.builder()
				.texture(new RenderState.TextureState(AllSpecialTextures.BLANK.getLocation(), false, false))
				.transparency(NO_TRANSPARENCY)
				.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
				.lightmap(ENABLE_LIGHTMAP)
				.overlay(ENABLE_OVERLAY_COLOR)
				.build(true));

	protected static final RenderState.CullState DISABLE_CULLING = new NoCullState();

	protected static class NoCullState extends RenderState.CullState {
		public NoCullState() {
			super(false);
		}

		@Override
		public void startDrawing() {
			RenderSystem.disableCull();
		}
	}

	// Mmm gimme those protected fields
	public RenderTypes() {
		super(null, null, null);
	}
}
