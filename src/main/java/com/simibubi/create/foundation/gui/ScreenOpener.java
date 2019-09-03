package com.simibubi.create.foundation.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ScreenOpener {

	@OnlyIn(Dist.CLIENT)
	private static Screen openedGuiNextTick;

	public static void tick() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			if (openedGuiNextTick != null) {
				Minecraft.getInstance().displayGuiScreen(openedGuiNextTick);
				openedGuiNextTick = null;
			}
		});
	}

	public static void open(Screen gui) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			openedGuiNextTick = gui;
		});
	}

}
