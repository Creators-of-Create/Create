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
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

// TODO 1.17: use custom shaders instead of vanilla ones
public class RenderTypes extends RenderStateShard {

	public static final RenderStateShard.ShaderStateShard GLOWING_SHADER = new RenderStateShard.ShaderStateShard(() -> Shaders.glowingShader);

	private static final RenderType OUTLINE_SOLID =
		RenderType.create(createLayerName("outline_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
			false, RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(AllSpecialTextures.BLANK.getLocation(), false, false))
				.setCullState(CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(false));

	public static RenderType getOutlineSolid() {
		return OUTLINE_SOLID;
	}

	public static RenderType getOutlineTranslucent(ResourceLocation texture, boolean cull) {
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
	}

	public static RenderType getGlowingSolid(ResourceLocation texture) {
		return RenderType.create(createLayerName("glowing_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
			true, false, RenderType.CompositeState.builder()
				.setShaderState(GLOWING_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setCullState(CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));
	}

	private static final RenderType GLOWING_SOLID_DEFAULT = getGlowingSolid(InventoryMenu.BLOCK_ATLAS);

	public static RenderType getGlowingSolid() {
		return GLOWING_SOLID_DEFAULT;
	}

	public static RenderType getGlowingTranslucent(ResourceLocation texture) {
		return RenderType.create(createLayerName("glowing_translucent"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
			256, true, true, RenderType.CompositeState.builder()
				.setShaderState(GLOWING_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));
	}

	private static final RenderType ADDITIVE = RenderType.create(createLayerName("additive"), DefaultVertexFormat.BLOCK,
		VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_SOLID_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
			.setTransparencyState(ADDITIVE_TRANSPARENCY)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	public static RenderType getAdditive() {
		return ADDITIVE;
	}

	private static final RenderType GLOWING_TRANSLUCENT_DEFAULT = getGlowingTranslucent(InventoryMenu.BLOCK_ATLAS);

	public static RenderType getGlowingTranslucent() {
		return GLOWING_TRANSLUCENT_DEFAULT;
	}

	private static final RenderType ITEM_PARTIAL_SOLID =
		RenderType.create(createLayerName("item_partial_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true,
			false, RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(BLOCK_SHEET)
				.setCullState(CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));

	public static RenderType getItemPartialSolid() {
		return ITEM_PARTIAL_SOLID;
	}

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderType.create(createLayerName("item_partial_translucent"),
		DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
			.setTextureState(BLOCK_SHEET)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	public static RenderType getItemPartialTranslucent() {
		return ITEM_PARTIAL_TRANSLUCENT;
	}

	public static BiFunction<ResourceLocation, Boolean, RenderType> TRAIN_MAP = Util.memoize(RenderTypes::getTrainMap);

	private static RenderType getTrainMap(ResourceLocation locationIn, boolean linearFiltering) {
		RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_TEXT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(locationIn, linearFiltering, false))
			.setTransparencyState(NO_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.createCompositeState(false);
		return RenderType.create("create_train_map", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
			false, true, rendertype$state);
	}

	private static final RenderType FLUID = RenderType.create(createLayerName("fluid"),
		DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true));

	public static RenderType getFluid() {
		return FLUID;
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
