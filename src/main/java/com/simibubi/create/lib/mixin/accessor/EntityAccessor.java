package com.simibubi.create.lib.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(Entity.class)
public interface EntityAccessor {
	@Accessor("removalReason")
	void create$setRemovalReason(Entity.RemovalReason removalReason);

	@Accessor("onGround")
	boolean create$isOnGround();

	@Invoker("getEncodeId")
	String create$getEntityString();

	@Invoker("collideWithShapes")
	static Vec3 create$collideWithShapes(Vec3 vec3, AABB aABB, List<VoxelShape> list) {
		throw new AssertionError("Mixin application failed!");
	}
}
