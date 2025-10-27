package com.simibubi.create.compat.trainmap;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.compat.Mods;

import net.minecraft.client.Minecraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class TrainMapEvents {

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.tick();
		if (Mods.JOURNEYMAP.isLoaded())
			JourneyTrainMap.tick();
		if (Mods.XAEROWORLDMAP.isLoaded())
			XaeroTrainMap.tick();
	}

	@SubscribeEvent
	public static void mouseClick(InputEvent.MouseButton.Pre event) {
		if (event.getAction() != InputConstants.PRESS)
			return;

		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.mouseClick(event);
		if (Mods.JOURNEYMAP.isLoaded())
			JourneyTrainMap.mouseClick(event);
		if (Mods.XAEROWORLDMAP.isLoaded())
			XaeroTrainMap.mouseClick(event);
	}

	@SubscribeEvent
	public static void cancelTooltips(RenderTooltipEvent.Pre event) {
		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.cancelTooltips(event);
	}

	@SubscribeEvent
	public static void renderGui(ScreenEvent.Render.Post event) {
		if (Mods.FTBCHUNKS.isLoaded())
			FTBChunksTrainMap.renderGui(event);
	}
}
