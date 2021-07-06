package com.simibubi.create.foundation.renderState;

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
			.texture(new RenderState.TextureState(texture, false, false))
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
			.alpha(ONE_TENTH_ALPHA)
			.cull(cull ? ENABLE_CULLING : DISABLE_CULLING)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true);
		return RenderType.of(createLayerName("outline_translucent" + (cull ? "_cull" : "")),
			DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS, 256, true, true, rendertype$state);
	}

	private static final RenderType OUTLINE_SOLID =
		RenderType.of(createLayerName("outline_solid"), DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS, 256, true,
			false, RenderType.State.builder()
				.texture(new RenderState.TextureState(AllSpecialTextures.BLANK.getLocation(), false, false))
				.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
				.lightmap(ENABLE_LIGHTMAP)
				.overlay(ENABLE_OVERLAY_COLOR)
				.build(true));

	public static RenderType getGlowingSolid(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.texture(new RenderState.TextureState(texture, false, false))
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true);
		return RenderType.of(createLayerName("glowing_solid"), DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS, 256,
			true, false, rendertype$state);
	}

	public static RenderType getGlowingTranslucent(ResourceLocation texture) {
		RenderType.State rendertype$state = RenderType.State.builder()
			.texture(new RenderState.TextureState(texture, false, false))
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.alpha(ONE_TENTH_ALPHA)
			.cull(DISABLE_CULLING)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true);
		return RenderType.of(createLayerName("glowing_translucent"), DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS,
			256, true, true, rendertype$state);
	}

	private static final RenderType GLOWING_SOLID = getGlowingSolid(PlayerContainer.BLOCK_ATLAS_TEXTURE);
	private static final RenderType GLOWING_TRANSLUCENT = getGlowingTranslucent(PlayerContainer.BLOCK_ATLAS_TEXTURE);

	private static final RenderType ITEM_PARTIAL_SOLID =
		RenderType.of(createLayerName("item_solid"), DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS, 256, true,
			false, RenderType.State.builder()
				.texture(BLOCK_ATLAS_TEXTURE)
				.transparency(NO_TRANSPARENCY)
				.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
				.lightmap(ENABLE_LIGHTMAP)
				.overlay(ENABLE_OVERLAY_COLOR)
				.build(true));

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.of(createLayerName("item_translucent"),
		DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS, 256, true, true, RenderType.State.builder()
			.texture(BLOCK_ATLAS_TEXTURE)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.diffuseLighting(ENABLE_DIFFUSE_LIGHTING)
			.alpha(ONE_TENTH_ALPHA)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true));

	private static final RenderType FLUID = RenderType.of(createLayerName("fluid"),
		DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, GL11.GL_QUADS, 256, true, true, RenderType.State.builder()
			.texture(MIPMAP_BLOCK_ATLAS_TEXTURE)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.shadeModel(SMOOTH_SHADE_MODEL)
			.alpha(ONE_TENTH_ALPHA)
			.lightmap(ENABLE_LIGHTMAP)
			.overlay(ENABLE_OVERLAY_COLOR)
			.build(true));

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
