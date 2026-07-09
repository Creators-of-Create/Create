package com.simibubi.create.compat.curios;

import com.simibubi.create.AllItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

public class CuriosRenderers {
	public static void register() {
		CuriosRendererRegistry.register(AllItems.GOGGLES.get(), () -> new GogglesCurioRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(GogglesCurioRenderer.LAYER)));
	}

	public static void onLayerRegister(final EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(GogglesCurioRenderer.LAYER, () -> LayerDefinition.create(GogglesCurioRenderer.mesh(), 1, 1));
	}
}
