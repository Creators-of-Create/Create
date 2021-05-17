package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.core.WorldContext;
import com.jozufozu.flywheel.backend.core.materials.ModelData;
import com.jozufozu.flywheel.backend.core.materials.OrientedData;
import com.jozufozu.flywheel.backend.gl.shader.ShaderCallback;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class InstancedTileRenderer<P extends BasicProgram> {

	public final WorldContext<P> context;

	protected final Map<MaterialSpec<?>, RenderMaterial<P, ?>> materials;

	protected final ArrayList<TileEntity> queuedAdditions;
	protected final ConcurrentHashMap.KeySetView<TileEntity, Boolean> queuedUpdates;

	protected final Map<TileEntity, TileEntityInstance<?>> instances;
	protected final Map<TileEntity, ITickableInstance> tickableInstances;
	protected final Map<TileEntity, IDynamicInstance> dynamicInstances;

	protected int frame;
	protected int tick;

	protected InstancedTileRenderer(WorldContext<P> context) {
		this.context = context;

		materials = new HashMap<>();
		for (MaterialSpec<?> spec : Backend.allMaterials()) {
			materials.put(spec, new RenderMaterial<>(this, spec));
		}

		queuedUpdates = ConcurrentHashMap.newKeySet(64);
		queuedAdditions = new ArrayList<>(64);
		dynamicInstances = new HashMap<>();
		tickableInstances = new HashMap<>();
		instances = new HashMap<>();
	}

	public abstract BlockPos getOriginCoordinate();

	public void tick(double cameraX, double cameraY, double cameraZ) {
		tick++;

		// integer camera pos
		int cX = (int) cameraX;
		int cY = (int) cameraY;
		int cZ = (int) cameraZ;

		if (tickableInstances.size() > 0) {
			for (ITickableInstance instance : tickableInstances.values()) {
				if (!instance.decreaseTickRateWithDistance()) {
					instance.tick();
					continue;
				}

				BlockPos pos = instance.getWorldPosition();

				int dX = pos.getX() - cX;
				int dY = pos.getY() - cY;
				int dZ = pos.getZ() - cZ;

				if ((tick % getUpdateDivisor(dX, dY, dZ)) == 0)
					instance.tick();
			}
		}

		queuedUpdates.forEach(te -> {
			queuedUpdates.remove(te);

			update(te);
		});
	}

	public void beginFrame(ActiveRenderInfo info) {
		frame++;
		processQueuedAdditions();

		Vector3f look = info.getHorizontalPlane();
		float lookX = look.getX();
		float lookY = look.getY();
		float lookZ = look.getZ();

		// integer camera pos
		int cX = (int) info.getProjectedView().x;
		int cY = (int) info.getProjectedView().y;
		int cZ = (int) info.getProjectedView().z;

		if (dynamicInstances.size() > 0) {
			for (IDynamicInstance dyn : dynamicInstances.values()) {
				if (!dyn.decreaseFramerateWithDistance() || shouldTick(dyn.getWorldPosition(), lookX, lookY, lookZ, cX, cY, cZ))
					dyn.beginFrame();
			}
		}
	}

	/**
	 * Render every model for every material.
	 *
	 * @param layer          Which vanilla {@link RenderType} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 */
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ) {
		render(layer, viewProjection, camX, camY, camZ, null);
	}

	/**
	 * Render every model for every material.
	 *
	 * @param layer          Which vanilla {@link RenderType} is being drawn?
	 * @param viewProjection How do we get from camera space to clip space?
	 * @param callback       Provide additional uniforms or state here.
	 */
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, ShaderCallback<P> callback) {
		for (RenderMaterial<P, ?> material : materials.values()) {
			material.render(layer, viewProjection, camX, camY, camZ, callback);
		}
	}

	@SuppressWarnings("unchecked")
	public <D extends InstanceData> RenderMaterial<P, D> getMaterial(MaterialSpec<D> materialType) {
		return (RenderMaterial<P, D>) materials.get(materialType);
	}

	public RenderMaterial<P, ModelData> getTransformMaterial() {
		return getMaterial(AllMaterialSpecs.TRANSFORMED);
	}

	public RenderMaterial<P, OrientedData> getOrientedMaterial() {
		return getMaterial(AllMaterialSpecs.ORIENTED);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends TileEntity> TileEntityInstance<? super T> getInstance(T tile, boolean create) {
		if (!Backend.canUseInstancing()) return null;

		TileEntityInstance<?> instance = instances.get(tile);

		if (instance != null) {
			return (TileEntityInstance<? super T>) instance;
		} else if (create && canCreateInstance(tile)) {
			return createInternal(tile);
		} else {
			return null;
		}
	}

	public <T extends TileEntity> void onLightUpdate(T tile) {
		if (!Backend.canUseInstancing()) return;

		if (tile instanceof IInstanceRendered) {
			TileEntityInstance<? super T> instance = getInstance(tile, false);

			if (instance != null)
				instance.updateLight();
		}
	}

	public <T extends TileEntity> void add(T tile) {
		if (!Backend.canUseInstancing()) return;

		if (tile instanceof IInstanceRendered) {
			addInternal(tile);
		}
	}

	public <T extends TileEntity> void update(T tile) {
		if (!Backend.canUseInstancing()) return;

		if (tile instanceof IInstanceRendered) {
			TileEntityInstance<? super T> instance = getInstance(tile, false);

			if (instance != null) {

				if (instance.shouldReset()) {
					removeInternal(tile, instance);

					createInternal(tile);
				} else {
					instance.update();
				}
			}
		}
	}

	public <T extends TileEntity> void remove(T tile) {
		if (!Backend.canUseInstancing()) return;

		if (tile instanceof IInstanceRendered) {
			removeInternal(tile);
		}
	}

	public synchronized <T extends TileEntity> void queueAdd(T tile) {
		if (!Backend.canUseInstancing()) return;

		queuedAdditions.add(tile);
	}

	public synchronized <T extends TileEntity> void queueUpdate(T tile) {
		if (!Backend.canUseInstancing()) return;

		queuedUpdates.add(tile);
	}

	protected synchronized void processQueuedAdditions() {
		if (queuedAdditions.size() > 0) {
			queuedAdditions.forEach(this::addInternal);
			queuedAdditions.clear();
		}
	}

	protected boolean shouldTick(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		int dX = worldPos.getX() - cX;
		int dY = worldPos.getY() - cY;
		int dZ = worldPos.getZ() - cZ;

		float dot = (dX + lookX * 2) * lookX + (dY + lookY * 2) * lookY + (dZ + lookZ * 2) * lookZ;

		if (dot < 0) return false; // is it more than 2 blocks behind the camera?

		return (frame % getUpdateDivisor(dX, dY, dZ)) == 0;
	}

	protected int getUpdateDivisor(int dX, int dY, int dZ) {
		int dSq = dX * dX + dY * dY + dZ * dZ;

		return (dSq / 1024) + 1;
	}

	private void addInternal(TileEntity tile) {
		getInstance(tile, true);
	}

	private <T extends TileEntity> void removeInternal(T tile) {
		TileEntityInstance<? super T> instance = getInstance(tile, false);

		if (instance != null) {
			removeInternal(tile, instance);
		}
	}

	private void removeInternal(TileEntity tile, TileEntityInstance<?> instance) {
		instance.remove();
		instances.remove(tile);
		dynamicInstances.remove(tile);
		tickableInstances.remove(tile);
	}

	private <T extends TileEntity> TileEntityInstance<? super T> createInternal(T tile) {
		TileEntityInstance<? super T> renderer = InstancedTileRenderRegistry.instance.create(this, tile);

		if (renderer != null) {
			renderer.updateLight();
			instances.put(tile, renderer);

			if (renderer instanceof IDynamicInstance)
				dynamicInstances.put(tile, (IDynamicInstance) renderer);

			if (renderer instanceof ITickableInstance)
				tickableInstances.put(tile, ((ITickableInstance) renderer));
		}

		return renderer;
	}

	public void invalidate() {
		for (RenderMaterial<?, ?> material : materials.values()) {
			material.delete();
		}
		instances.clear();
		dynamicInstances.clear();
		tickableInstances.clear();
	}

	public boolean canCreateInstance(TileEntity tile) {
		if (tile.isRemoved()) return false;

		World world = tile.getWorld();

		if (world == null) return false;

		if (world.isAirBlock(tile.getPos())) return false;

		if (world == Minecraft.getInstance().world) {
			BlockPos pos = tile.getPos();

			IBlockReader existingChunk = world.getExistingChunk(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return world instanceof IFlywheelWorld && ((IFlywheelWorld) world).supportsFlywheel();
	}
}
