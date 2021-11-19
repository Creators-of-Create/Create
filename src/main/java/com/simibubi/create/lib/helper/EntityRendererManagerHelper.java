package com.simibubi.create.lib.helper;

import java.util.Map;

import com.simibubi.create.lib.mixin.accessor.EntityRenderDispatcherAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

public class EntityRendererManagerHelper {
	public static PlayerRenderer getPlayerRenderer(EntityRenderDispatcher manager) {
		return (PlayerRenderer) manager.getRenderer(Minecraft.getInstance().player);
		//return ((EntityRenderDispatcherAccessor) manager).getPlayerRenderer();
	}

	public static Map<String, EntityRenderer<? extends Player>> getSkinMap(EntityRenderDispatcher manager) {
		return ((EntityRenderDispatcherAccessor) manager).getPlayerRenderers();
	}

	public static Map<EntityType<?>, EntityRenderer<?>> getRenderers(EntityRenderDispatcher manager) {
		return ((EntityRenderDispatcherAccessor) manager).getRenderers();
	}
}
