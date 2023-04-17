package com.simibubi.create.compat.curios;

import com.simibubi.create.AllItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@OnlyIn(Dist.CLIENT)
public class CuriosRenderers {
	public static void register() {
		CuriosRendererRegistry.register(AllItems.GOGGLES.get(), () -> new GogglesCurioRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(GogglesCurioRenderer.LAYER)));
		CuriosRendererRegistry.register(AllItems.COPPER_BACKTANK.get(), () -> new CopperBacktankCurioRenderer(Minecraft.getInstance().getEntityModels().bakeLayer(CopperBacktankCurioRenderer.LAYER)));
	}

	public static void onLayerRegister(final EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(GogglesCurioRenderer.LAYER, () -> LayerDefinition.create(GogglesCurioRenderer.mesh(), 1, 1));
		event.registerLayerDefinition(CopperBacktankCurioRenderer.LAYER, () -> LayerDefinition.create(CopperBacktankCurioRenderer.mesh(), 1, 1));
	}
}
