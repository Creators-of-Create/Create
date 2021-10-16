package com.simibubi.create.foundation.render;

import org.lwjgl.opengl.GL11;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.Create;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class RenderTypes extends RenderState {

	public static RenderType getOutlineTranslucent(ResourceLocation texture, boolean cull) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(texture, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(cull ? CULL : NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return RenderType.create(createLayerName("outline_translucent" + (cull ? "_cull" : "")),
			DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, true, rendertype$state);
	}

	private static final RenderType OUTLINE_SOLID =
		RenderType.create(createLayerName("outline_solid"), DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true,
			false, RenderType.State.builder()
				.setTextureState(new RenderState.TextureState(AllSpecialTextures.BLANK.getLocation(), false, false))
				.setDiffuseLightingState(DIFFUSE_LIGHTING)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	public static RenderType getGlowingSolid(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(texture, false, false))
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return RenderType.create(createLayerName("glowing_solid"), DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256,
			true, false, rendertype$state);
	}

	public static RenderType getGlowingTranslucent(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.setTextureState(new RenderState.TextureState(texture, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return RenderType.create(createLayerName("glowing_translucent"), DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS,
			256, true, true, rendertype$state);
	}

	private static final RenderType GLOWING_SOLID = getGlowingSolid(PlayerContainer.BLOCK_ATLAS);
	private static final RenderType GLOWING_TRANSLUCENT = getGlowingTranslucent(PlayerContainer.BLOCK_ATLAS);

	private static final RenderType ITEM_PARTIAL_SOLID =
		RenderType.create(createLayerName("item_solid"), DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true,
			false, RenderType.State.builder()
				.setTextureState(BLOCK_SHEET)
				.setTransparencyState(NO_TRANSPARENCY)
				.setDiffuseLightingState(DIFFUSE_LIGHTING)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.create(createLayerName("item_translucent"),
		DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, true, RenderType.State.builder()
			.setTextureState(BLOCK_SHEET)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	private static final RenderType FLUID = RenderType.create(createLayerName("fluid"),
		DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, true, RenderType.State.builder()
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setShadeModelState(SMOOTH_SHADE)
			.setAlphaState(DEFAULT_ALPHA)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	private static String createLayerName(String name) {
		return Create.ID + ":" + name;
	}

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

	public static RenderType getFluid() {
		return FLUID;
	}

	// Mmm gimme those protected fields
	public RenderTypes() {
		super(null, null, null);
	}

}
