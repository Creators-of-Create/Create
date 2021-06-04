package com.jozufozu.flywheel.event;

import java.util.ArrayList;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.TileInstanceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ForgeEvents {

	@SubscribeEvent
	public static void addToDebugScreen(RenderGameOverlayEvent.Text event) {

		if (Minecraft.getInstance().gameSettings.showDebugInfo) {

			ArrayList<String> right = event.getRight();

			String text = "Flywheel: " + Backend.getBackendDescriptor();
			if (right.size() < 10) {
				right.add("");
				right.add(text);
			} else {
				right.add(9, "");
				right.add(10, text);
			}
		}
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		IWorld world = event.getWorld();

		if (Backend.isFlywheelWorld(world)) {
			TileInstanceManager renderer = InstancedRenderDispatcher.get(world);
			renderer.invalidate();
			((ClientWorld) world).loadedTileEntityList.forEach(renderer::add);
		}
	}
}
