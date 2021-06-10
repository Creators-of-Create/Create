package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;

public abstract class InstanceManager<T> implements MaterialManager.OriginShiftListener {

	public final MaterialManager<?> materialManager;

	protected final ArrayList<T> queuedAdditions;
	protected final ConcurrentHashMap.KeySetView<T, Boolean> queuedUpdates;

	protected final Map<T, IInstance> instances;
	protected final Object2ObjectOpenHashMap<T, ITickableInstance> tickableInstances;
	protected final Object2ObjectOpenHashMap<T, IDynamicInstance> dynamicInstances;

	protected int frame;
	protected int tick;

	public InstanceManager(MaterialManager<?> materialManager) {
		this.materialManager = materialManager;
		this.queuedUpdates = ConcurrentHashMap.newKeySet(64);
		this.queuedAdditions = new ArrayList<>(64);
		this.instances = new HashMap<>();

		this.dynamicInstances = new Object2ObjectOpenHashMap<>();
		this.tickableInstances = new Object2ObjectOpenHashMap<>();

		materialManager.onOriginShift(this);
	}

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
			dynamicInstances.object2ObjectEntrySet().fastForEach(e -> {
				IDynamicInstance dyn = e.getValue();
				if (!dyn.decreaseFramerateWithDistance() || shouldFrameUpdate(dyn.getWorldPosition(), lookX, lookY, lookZ, cX, cY, cZ))
					dyn.beginFrame();
			});
		}
	}

	public void add(T obj) {
		if (!Backend.getInstance().canUseInstancing()) return;

		if (obj instanceof IInstanceRendered) {
			addInternal(obj);
		}
	}

	public synchronized void queueAdd(T obj) {
		if (!Backend.getInstance().canUseInstancing()) return;

		queuedAdditions.add(obj);
	}

	public void update(T obj) {
		if (!Backend.getInstance().canUseInstancing()) return;

		if (obj instanceof IInstanceRendered) {
			IInstance instance = getInstance(obj, false);

			if (instance != null) {

				if (instance.shouldReset()) {
					removeInternal(obj, instance);

					createInternal(obj);
				} else {
					instance.update();
				}
			}
		}
	}

	public synchronized void queueUpdate(T obj) {
		if (!Backend.getInstance().canUseInstancing()) return;

		queuedUpdates.add(obj);
	}

	public void onLightUpdate(T obj) {
		if (!Backend.getInstance().canUseInstancing()) return;

		if (obj instanceof IInstanceRendered) {
			IInstance instance = getInstance(obj, false);

			if (instance != null)
				instance.updateLight();
		}
	}

	public void remove(T obj) {
		if (!Backend.getInstance().canUseInstancing()) return;

		if (obj instanceof IInstanceRendered) {
			IInstance instance = getInstance(obj, false);
			if (instance != null)
				removeInternal(obj, instance);
		}
	}

	public void invalidate() {
		instances.clear();
		dynamicInstances.clear();
		tickableInstances.clear();
	}

	@SuppressWarnings("unchecked")
	@Nullable
	protected <I extends T> IInstance getInstance(I obj, boolean create) {
		if (!Backend.getInstance().canUseInstancing()) return null;

		IInstance instance = instances.get(obj);

		if (instance != null) {
			return instance;
		} else if (create && canCreateInstance(obj)) {
			return createInternal(obj);
		} else {
			return null;
		}
	}

	protected synchronized void processQueuedAdditions() {
		if (queuedAdditions.size() > 0) {
			queuedAdditions.forEach(this::addInternal);
			queuedAdditions.clear();
		}
	}

	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		int dX = worldPos.getX() - cX;
		int dY = worldPos.getY() - cY;
		int dZ = worldPos.getZ() - cZ;

		// is it more than 2 blocks behind the camera?
		int dist = 2;
		float dot = (dX + lookX * dist) * lookX + (dY + lookY * dist) * lookY + (dZ + lookZ * dist) * lookZ;
		if (dot < 0) return false;

		return (frame % getUpdateDivisor(dX, dY, dZ)) == 0;
	}

	protected int getUpdateDivisor(int dX, int dY, int dZ) {
		int dSq = dX * dX + dY * dY + dZ * dZ;

		return (dSq / 1024) + 1;
	}

	protected void addInternal(T tile) {
		getInstance(tile, true);
	}

	protected void removeInternal(T obj, IInstance instance) {
		instance.remove();
		instances.remove(obj);
		dynamicInstances.remove(obj);
		tickableInstances.remove(obj);
	}

	protected IInstance createInternal(T obj) {
		IInstance renderer = createRaw(obj);

		if (renderer != null) {
			renderer.updateLight();
			instances.put(obj, renderer);

			if (renderer instanceof IDynamicInstance)
				dynamicInstances.put(obj, (IDynamicInstance) renderer);

			if (renderer instanceof ITickableInstance)
				tickableInstances.put(obj, ((ITickableInstance) renderer));
		}

		return renderer;
	}

	@Override
	public void onOriginShift() {
		ArrayList<T> instancedTiles = new ArrayList<>(instances.keySet());
		invalidate();
		instancedTiles.forEach(this::add);
	}

	protected abstract IInstance createRaw(T obj);

	protected abstract boolean canCreateInstance(T entity);
}
