package com.simibubi.create.foundation.render;

import java.io.IOException;
import java.util.function.BiFunction;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.Create;

import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

public class RenderTypes extends RenderStateShard {
	public static final RenderStateShard.ShaderStateShard GLOWING_SHADER = new RenderStateShard.ShaderStateShard(() -> Shaders.glowingShader);

	private static final RenderType ENTITY_SOLID_BLOCK_MIPPED = RenderType.create(createLayerName("entity_solid_block_mipped"),
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(BLOCK_SHEET_MIPPED)
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	private static final RenderType ENTITY_CUTOUT_BLOCK_MIPPED = RenderType.create(createLayerName("entity_cutout_block_mipped"),
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
				.setTextureState(BLOCK_SHEET_MIPPED)
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	private static final RenderType ENTITY_TRANSLUCENT_BLOCK_MIPPED = RenderType.create(createLayerName("entity_translucent_block_mipped"),
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
				.setTextureState(BLOCK_SHEET_MIPPED)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	private static final RenderType ADDITIVE = RenderType.create(createLayerName("additive"), DefaultVertexFormat.BLOCK,
		VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_SOLID_SHADER)
			.setTextureState(BLOCK_SHEET)
			.setTransparencyState(ADDITIVE_TRANSPARENCY)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	private static final RenderType FLUID = RenderType.create(createLayerName("fluid"),
		DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setCullState(NO_CULL)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	private static final RenderType ITEM_GLOWING_SOLID = RenderType.create(createLayerName("item_glowing_solid"),
		DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, RenderType.CompositeState.builder()
			.setShaderState(GLOWING_SHADER)
			.setTextureState(BLOCK_SHEET)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	private static final RenderType ITEM_GLOWING_TRANSLUCENT = RenderType.create(createLayerName("item_glowing_translucent"),
		DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
			.setShaderState(GLOWING_SHADER)
			.setTextureState(BLOCK_SHEET)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	private static final RenderType OUTLINE_SOLID =
		RenderType.create(createLayerName("outline_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
			false, RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(AllSpecialTextures.BLANK.getLocation(), false, false))
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(false));

	private static final BiFunction<ResourceLocation, Boolean, RenderType> OUTLINE_TRANSLUCENT = Util.memoize((texture, cull) -> {
		return RenderType.create(createLayerName("outline_translucent" + (cull ? "_cull" : "")),
			DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
				.setShaderState(cull ? RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER : RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(cull ? CULL : NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.setWriteMaskState(COLOR_WRITE)
				.createCompositeState(false));
	});

	public static RenderType entitySolidBlockMipped() {
		return ENTITY_SOLID_BLOCK_MIPPED;
	}

	public static RenderType entityCutoutBlockMipped() {
		return ENTITY_CUTOUT_BLOCK_MIPPED;
	}

	public static RenderType entityTranslucentBlockMipped() {
		return ENTITY_TRANSLUCENT_BLOCK_MIPPED;
	}

	public static RenderType additive() {
		return ADDITIVE;
	}

	public static RenderType fluid() {
		return FLUID;
	}

	public static RenderType itemGlowingSolid() {
		return ITEM_GLOWING_SOLID;
	}

	public static RenderType itemGlowingTranslucent() {
		return ITEM_GLOWING_TRANSLUCENT;
	}

	public static RenderType outlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType outlineTranslucent(ResourceLocation texture, boolean cull) {
		return OUTLINE_TRANSLUCENT.apply(texture, cull);
	}

	private static String createLayerName(String name) {
		return Create.ID + ":" + name;
	}

	// Mmm gimme those protected fields
	private RenderTypes() {
		super(null, null, null);
	}

	@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	private static class Shaders {
		private static ShaderInstance glowingShader;

		@SubscribeEvent
		public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
			ResourceProvider resourceProvider = event.getResourceProvider();
			event.registerShader(new ShaderInstance(resourceProvider, Create.asResource("glowing_shader"), DefaultVertexFormat.NEW_ENTITY), shader -> glowingShader = shader);
		}
	}
}
