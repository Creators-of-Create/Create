package com.jozufozu.flywheel.backend.instancing.tile;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.IInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class TileInstanceManager extends InstanceManager<TileEntity> {

	public TileInstanceManager(MaterialManager<?> materialManager) {
		super(materialManager);
	}

	@Override
	protected IInstance createRaw(TileEntity obj) {
		return InstancedRenderRegistry.getInstance().create(materialManager, obj);
	}

	@Override
	protected boolean canCreateInstance(TileEntity tile) {
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
