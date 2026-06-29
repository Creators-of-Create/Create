package com.simibubi.create.api.contraption.dispenser;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.mixin.accessor.ProjectileDispenseBehaviorAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;

/**
 * A parallel to {@link ProjectileDispenseBehavior}, providing a base implementation for projectile-shooting behaviors.
 */
public abstract class MountedProjectileDispenseBehavior extends DefaultMountedDispenseBehavior {
	@Override
	protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3 facing) {
		double x = pos.getX() + facing.x * .7 + .5;
		double y = pos.getY() + facing.y * .7 + .5;
		double z = pos.getZ() + facing.z * .7 + .5;
		Projectile projectile = this.getProjectile(context.world, x, y, z, stack.copy(), MountedDispenseBehavior.getClosestFacingDirection(facing));
		if (projectile == null)
			return stack;

		Vec3 motion = facing.scale(this.getPower()).add(context.motion);
		projectile.shoot(motion.x, motion.y, motion.z, (float) motion.length(), this.getUncertainty());
		context.world.addFreshEntity(projectile);
		stack.shrink(1);
		return stack;
	}

	@Override
	protected void playSound(LevelAccessor level, BlockPos pos) {
		level.levelEvent(LevelEvent.SOUND_DISPENSER_PROJECTILE_LAUNCH, pos, 0);
	}

	@Nullable
	protected abstract Projectile getProjectile(Level level, double x, double y, double z, ItemStack stack, Direction facing);

	protected float getUncertainty() {
		return 6;
	}

	protected float getPower() {
		return 1.1f;
	}

	/**
	 * Create a mounted behavior wrapper from a vanilla projectile dispense behavior.
	 */
	public static MountedDispenseBehavior of(ProjectileDispenseBehavior vanillaBehaviour) {
		ProjectileDispenseBehaviorAccessor accessor = (ProjectileDispenseBehaviorAccessor) vanillaBehaviour;
		return new MountedProjectileDispenseBehavior() {
			@Override
			protected Projectile getProjectile(Level level, double x, double y, double z, ItemStack stack, Direction facing) {
				return accessor.create$getProjectileItem().asProjectile(level, new Vec3(x, y, z), stack, facing);
			}

			@Override
			protected float getUncertainty() {
				return accessor.create$getDispenseConfig().uncertainty();
			}

			@Override
			protected float getPower() {
				return accessor.create$getDispenseConfig().power();
			}
		};
	}
}
