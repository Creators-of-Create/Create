package com.jozufozu.flywheel.event;

import java.util.ArrayList;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ForgeEvents {

	@SubscribeEvent
	public static void addToDebugScreen(RenderGameOverlayEvent.Text event) {

		if (Minecraft.getInstance().gameSettings.showDebugInfo) {

			ArrayList<String> right = event.getRight();

			String text = "Flywheel: " + Backend.getInstance().getBackendDescriptor();
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
			ClientWorld clientWorld = (ClientWorld) world;

			TileInstanceManager tiles = InstancedRenderDispatcher.getTiles(world);
			tiles.invalidate();
			clientWorld.loadedTileEntityList.forEach(tiles::add);

			EntityInstanceManager entities = InstancedRenderDispatcher.getEntities(world);
			entities.invalidate();
			clientWorld.getAllEntities().forEach(entities::add);
		}
	}
}
