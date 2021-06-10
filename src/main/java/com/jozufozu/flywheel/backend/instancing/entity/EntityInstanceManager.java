package com.jozufozu.flywheel.backend.instancing.entity;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class EntityInstanceManager extends InstanceManager<Entity> {

	public EntityInstanceManager(MaterialManager<?> materialManager) {
		super(materialManager);
	}

	@Override
	protected IInstance createRaw(Entity obj) {
		return InstancedRenderRegistry.getInstance().create(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(Entity entity) {
		if (!entity.isAlive()) return false;

		World world = entity.world;

		if (world == null) return false;

		if (Backend.isFlywheelWorld(world)) {
			BlockPos pos = entity.getBlockPos();

			IBlockReader existingChunk = world.getExistingChunk(pos.getX() >> 4, pos.getZ() >> 4);

			return existingChunk != null;
		}

		return false;
	}
}
