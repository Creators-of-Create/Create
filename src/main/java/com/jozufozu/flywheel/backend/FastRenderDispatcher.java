package com.jozufozu.flywheel.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jozufozu.flywheel.backend.core.BasicInstancedTileRenderer;
import com.jozufozu.flywheel.backend.core.OrientedModel;
import com.jozufozu.flywheel.backend.core.TransformedModel;
import com.jozufozu.flywheel.backend.instancing.MaterialFactory;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.content.contraptions.base.KineticRenderMaterials;
import com.simibubi.create.content.contraptions.base.RotatingModel;
import com.simibubi.create.content.contraptions.components.actors.ActorModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapModel;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

public class FastRenderDispatcher {

	public static Map<MaterialType<?>, MaterialFactory> materials = new HashMap<>();

	static {
		registerMaterials();
	}

	public static WorldAttached<ConcurrentHashMap.KeySetView<TileEntity, Boolean>> queuedUpdates = new WorldAttached<>(ConcurrentHashMap::newKeySet);

	public static void registerMaterials() {
		materials.put(MaterialTypes.TRANSFORMED, new MaterialFactory(AllProgramSpecs.MODEL, TransformedModel::new));
		materials.put(MaterialTypes.ORIENTED, new MaterialFactory(AllProgramSpecs.ORIENTED, OrientedModel::new));
		materials.put(KineticRenderMaterials.BELTS, new MaterialFactory(AllProgramSpecs.BELT, BeltInstancedModel::new));
		materials.put(KineticRenderMaterials.ROTATING, new MaterialFactory(AllProgramSpecs.ROTATING, RotatingModel::new));
		materials.put(KineticRenderMaterials.FLAPS, new MaterialFactory(AllProgramSpecs.FLAPS, FlapModel::new));
		materials.put(KineticRenderMaterials.ACTORS, new MaterialFactory(AllProgramSpecs.C_ACTOR, ActorModel::new));
	}

	public static void enqueueUpdate(TileEntity te) {
		queuedUpdates.get(te.getWorld()).add(te);
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.world;

		BasicInstancedTileRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);

		Entity renderViewEntity = mc.renderViewEntity;
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
		BasicInstancedTileRenderer kineticRenderer = CreateClient.kineticRenderer.get(world);

		layer.startDrawing();

		kineticRenderer.render(layer, viewProjection, cameraX, cameraY, cameraZ);

		layer.endDrawing();
	}
}
