package com.simibubi.create.foundation.renderState;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllSpecialTextures;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderState {

	protected static final RenderState.CullState DISABLE_CULLING = new NoCullState();

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

	public static RenderType getGlowingSolid(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.texture(new RenderState.TextureState(texture, false, false))
			.transparency(NO_TRANSPARENCY)
			.diffuseLighting(DISABLE_DIFFUSE_LIGHTING)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true);
		return RenderType.of("glowing_solid", DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256,
			true, false, rendertype$state);
	}

	public static RenderType getGlowingTranslucent(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.texture(new RenderState.TextureState(texture, false, false))
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.diffuseLighting(DISABLE_DIFFUSE_LIGHTING)
			.alpha(ONE_TENTH_ALPHA)
			.cull(DISABLE_CULLING)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true);
		return RenderType.of("glowing_translucent", DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7,
			256, true, true, rendertype$state);
	}

	private static final RenderType GLOWING_SOLID = RenderTypes.getGlowingSolid(PlayerContainer.BLOCK_ATLAS_TEXTURE);
	private static final RenderType GLOWING_TRANSLUCENT =
		RenderTypes.getGlowingTranslucent(PlayerContainer.BLOCK_ATLAS_TEXTURE);

	private static final RenderType ITEM_PARTIAL_SOLID =
		RenderType.of("item_solid", DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true,
			false, RenderType.State.builder()
				.texture(new RenderState.TextureState(PlayerContainer.BLOCK_ATLAS_TEXTURE, false, false))
				.transparency(NO_TRANSPARENCY)
				.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
				.lightmap(ENABLE_LIGHTMAP)
				.overlay(ENABLE_OVERLAY_COLOR)
				.build(true));

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.of("entity_translucent",
		DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, RenderType.State.builder()
			.texture(new RenderState.TextureState(PlayerContainer.BLOCK_ATLAS_TEXTURE, false, false))
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
			.alpha(ONE_TENTH_ALPHA)
			.cull(DISABLE_CULLING)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true));

	public static RenderType getItemPartialSolid() {
		return ITEM_PARTIAL_SOLID;
	}
	
	public static RenderType getItemPartialTranslucent() {
		return ITEM_PARTIAL_TRANSLUCENT;
	}

	public static RenderType getOutlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType getGlowingSolid() {
		return GLOWING_SOLID;
	}

	public static RenderType getGlowingTranslucent() {
		return GLOWING_TRANSLUCENT;
	}

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
