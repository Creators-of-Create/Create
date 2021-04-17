package com.simibubi.create.content.optics;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public interface ILightHandler<T extends TileEntity & ILightHandler<T>> {
	Vector3d getBeamDirection();

	default double getBeamLenght() {
		T te = getTile();
		World world = te.getWorld();
		BlockPos pos = te.getPos();
		if (pos == BlockPos.ZERO || world == null)
			return 0;

		Vector3d direction = getBeamDirection();
		BlockRayTraceResult raytrace = world
				.rayTraceBlocks(new RayTraceContext(Vector3d.of(pos).add(direction),
						direction.normalize()
								.scale(128)
								.add(Vector3d.of(pos)),
						RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));

		return Vector3d.of(raytrace.getPos()
				.subtract(pos))
				.length();
	}

	T getTile();
}
