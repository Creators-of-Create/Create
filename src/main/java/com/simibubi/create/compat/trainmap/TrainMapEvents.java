package com.simibubi.create.compat.trainmap;

import com.mojang.blaze3d.platform.InputConstants;
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

	}

	@SubscribeEvent
	public static void mouseClick(InputEvent.MouseButton.Pre event) {
		if (event.getAction() != InputConstants.PRESS)
			return;

	}

	@SubscribeEvent
	public static void cancelTooltips(RenderTooltipEvent.Pre event) {
	}

	@SubscribeEvent
	public static void renderGui(ScreenEvent.Render.Post event) {
	}
}
