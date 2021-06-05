package com.simibubi.create.foundation.render.backend;

import java.util.concurrent.ConcurrentHashMap;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.foundation.render.KineticRenderer;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

public class FastRenderDispatcher {

	public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> queuedUpdates = new WorldAttached<>(ConcurrentHashMap::newKeySet);

	public static void enqueueUpdate(TileEntity te) {
		queuedUpdates.get(te.getWorld()).add(te);
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;

		KineticRenderer kineticRenderer = CreateClient.KINETIC_RENDERER.get(world);

		Entity renderViewEntity = mc.renderViewEntity != null ? mc.renderViewEntity : mc.player;
		kineticRenderer.tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());

		ConcurrentHashMap.KeySetView<TileEntity, Boolean> map = queuedUpdates.get(world);
		map
				.forEach(te -> {
					map.remove(te);

					kineticRenderer.update(te);
				});
	}

	public static boolean available() {
		return Backend.canUseInstancing();
	}

	public static boolean available(World world) {
		return Backend.canUseInstancing() && Backend.isFlywheelWorld(world);
	}

	public static int getDebugMode() {
		return KineticDebugger.isActive() ? 1 : 0;
	}

	public static void refresh() {
		RenderWork.enqueue(Minecraft.getInstance().worldRenderer::loadRenderers);
	}

	public static void renderLayer(RenderType layer, Matrix4f viewProjection, double cameraX, double cameraY, double cameraZ) {
		if (!Backend.canUseInstancing()) return;

		ClientWorld world = Minecraft.getInstance().world;
		KineticRenderer kineticRenderer = CreateClient.KINETIC_RENDERER.get(world);

		layer.startDrawing();

		kineticRenderer.render(layer, viewProjection, cameraX, cameraY, cameraZ);

		layer.endDrawing();
	}
}
