package com.simibubi.create.compat.sodium;

import java.util.function.Function;

import com.simibubi.create.Create;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

/**
 * Fixes the Mechanical Saw's sprite and Factory Gauge's sprite
 */
public class SodiumCompat {
	public static final ResourceLocation SAW_TEXTURE = Create.asResource("block/saw_reversed");
	public static final ResourceLocation FACTORY_PANEL_TEXTURE = Create.asResource("block/factory_panel_connections_animated");

	public static void init(IEventBus modEventBus, IEventBus neoEventBus) {
		Minecraft mc = Minecraft.getInstance();
		neoEventBus.addListener((RenderLevelStageEvent event) -> {
			if (event.getStage() == Stage.AFTER_ENTITIES) {
				Function<ResourceLocation, TextureAtlasSprite> atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
				TextureAtlasSprite sawSprite = atlas.apply(SAW_TEXTURE);
				SpriteUtil.INSTANCE.markSpriteActive(sawSprite);

				TextureAtlasSprite factoryPanelSprite = atlas.apply(FACTORY_PANEL_TEXTURE);
				SpriteUtil.INSTANCE.markSpriteActive(factoryPanelSprite);
			}
		});
	}
}
