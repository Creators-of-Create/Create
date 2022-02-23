package com.simibubi.create.foundation.render;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import com.jozufozu.flywheel.backend.IrisShaderHandler;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.Create;
import com.simibubi.create.lib.event.RegisterShadersCallback;
import com.simibubi.create.lib.mixin.client.accessor.RenderTypeAccessor;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;

// TODO 1.17: use custom shaders instead of vanilla ones
public class RenderTypes extends RenderStateShard {

	public static final RenderStateShard.ShaderStateShard GLOWING_SHADER = new RenderStateShard.ShaderStateShard(() -> IrisShaderHandler.isShaderPackInUse() ? GameRenderer.getNewEntityShader() : Shaders.glowingShader);

	private static final RenderType OUTLINE_SOLID =
		RenderTypeAccessor.create$create(createLayerName("outline_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
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
		return RenderTypeAccessor.create$create(createLayerName("outline_translucent" + (cull ? "_cull" : "")),
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
		return RenderTypeAccessor.create$create(createLayerName("glowing_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256,
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
		return RenderTypeAccessor.create$create(createLayerName("glowing_translucent"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
			256, true, true, RenderType.CompositeState.builder()
				.setShaderState(GLOWING_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true));
	}

	private static final RenderType GLOWING_TRANSLUCENT_DEFAULT = getGlowingTranslucent(InventoryMenu.BLOCK_ATLAS);

	public static RenderType getGlowingTranslucent() {
		return GLOWING_TRANSLUCENT_DEFAULT;
	}

	private static final RenderType ITEM_PARTIAL_SOLID =
		RenderTypeAccessor.create$create(createLayerName("item_partial_solid"), DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true,
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

	private static final RenderType ITEM_PARTIAL_TRANSLUCENT = RenderTypeAccessor.create$create(createLayerName("item_partial_translucent"),
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

	private static final RenderType FLUID = RenderTypeAccessor.create$create(createLayerName("fluid"),
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

	public static void init() {
		RegisterShadersCallback.EVENT.register(Shaders::onRegisterShaders);
	}

	private static class Shaders {
		private static ShaderInstance glowingShader;

		public static void onRegisterShaders(List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderRegistry, ResourceManager resourceManager) throws IOException {
			shaderRegistry.add(Pair.of(new ShaderInstance(resourceManager, Create.asResource("glowing_shader").toString(), DefaultVertexFormat.NEW_ENTITY), shader -> glowingShader = shader));
		}
	}

}
