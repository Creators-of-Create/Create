package com.simibubi.create.lib.mixin.compat.lithium;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.ponder.PonderWorld;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


@Mixin(Entity.class)
public abstract class EntityMixin {

	/**
	 * @author AeiouEnigma
	 * @reason We like Lithium's collision optimizations but need to ensure they aren't applied in Create's PonderWorld.
	 */
	@Inject(
			method = "collideBoundingBox",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void create$stopLithiumCollisionChangesInPonderWorld(@Nullable Entity entity, Vec3 movement, AABB entityBoundingBox, Level world, List<VoxelShape> shapes, CallbackInfoReturnable ci) {
		if (world instanceof PonderWorld) {
			// Vanilla copy
			ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(shapes.size() + 1);
			if (!shapes.isEmpty()) {
				builder.addAll(shapes);
			}

			WorldBorder worldBorder = world.getWorldBorder();
			boolean bl = entity != null && worldBorder.isInsideCloseToBorder(entity, entityBoundingBox.expandTowards(movement));
			if (bl) {
				builder.add(worldBorder.getCollisionShape());
			}

			builder.addAll(world.getBlockCollisions(entity, entityBoundingBox.expandTowards(movement)));
			ci.setReturnValue(collideWithShapes(movement, entityBoundingBox, builder.build()));
			// Prevent Lithium's changes from executing for PonderWorlds
			ci.cancel();
		}
	}

	@Shadow
	private static Vec3 collideWithShapes(Vec3 vec3, AABB aABB, List<VoxelShape> list) {
		// Vanilla copy
		if (list.isEmpty()) {
			return vec3;
		} else {
			double d = vec3.x;
			double e = vec3.y;
			double f = vec3.z;
			if (e != 0.0D) {
				e = Shapes.collide(Direction.Axis.Y, aABB, list, e);
				if (e != 0.0D) {
					aABB = aABB.move(0.0D, e, 0.0D);
				}
			}

			boolean bl = Math.abs(d) < Math.abs(f);
			if (bl && f != 0.0D) {
				f = Shapes.collide(Direction.Axis.Z, aABB, list, f);
				if (f != 0.0D) {
					aABB = aABB.move(0.0D, 0.0D, f);
				}
			}

			if (d != 0.0D) {
				d = Shapes.collide(Direction.Axis.X, aABB, list, d);
				if (!bl && d != 0.0D) {
					aABB = aABB.move(d, 0.0D, 0.0D);
				}
			}

			if (!bl && f != 0.0D) {
				f = Shapes.collide(Direction.Axis.Z, aABB, list, f);
			}

			return new Vec3(d, e, f);
		}
	}
}
