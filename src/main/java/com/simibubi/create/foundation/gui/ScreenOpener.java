package com.simibubi.create.foundation.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class ScreenOpener {

	private static Screen openedGuiNextTick;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		if (openedGuiNextTick != null) {
			Minecraft.getInstance().displayGuiScreen(openedGuiNextTick);
			openedGuiNextTick = null;
		}
	}
	
	public static void open(Screen gui) {
		openedGuiNextTick = gui;
	}
	
}
