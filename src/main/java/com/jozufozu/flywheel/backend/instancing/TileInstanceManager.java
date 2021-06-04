package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class TileInstanceManager implements MaterialManager.OriginShiftListener {

	public final MaterialManager<?> materialManager;

	protected final ArrayList<TileEntity> queuedAdditions;
	protected final ConcurrentHashMap.KeySetView<TileEntity, Boolean> queuedUpdates;

	protected final Map<TileEntity, TileEntityInstance<?>> instances;
	protected final Object2ObjectOpenHashMap<TileEntity, ITickableInstance> tickableInstances;
	protected final Object2ObjectOpenHashMap<TileEntity, IDynamicInstance> dynamicInstances;

	protected int frame;
	protected int tick;

	public TileInstanceManager(MaterialManager<?> materialManager) {
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

	@Override
	public void onOriginShift() {
		ArrayList<TileEntity> instancedTiles = new ArrayList<>(instances.keySet());
		invalidate();
		instancedTiles.forEach(this::add);
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
		TileEntityInstance<? super T> renderer = InstancedRenderRegistry.getInstance().create(materialManager, tile);

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
		instances.clear();
		dynamicInstances.clear();
		tickableInstances.clear();
	}

	public boolean canCreateInstance(TileEntity tile) {
		if (tile.isRemoved()) return false;

		World world = tile.getWorld();

		if (world == null) return false;

		if (world.isAirBlock(tile.getPos())) return false;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = tile.getPos();

			IBlockReader existingChunk = world.getExistingChunk(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
