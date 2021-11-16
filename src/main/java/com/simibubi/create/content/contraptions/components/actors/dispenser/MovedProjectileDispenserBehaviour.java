package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.lib.utility.MethodGetter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public abstract class MovedProjectileDispenserBehaviour extends MovedDefaultDispenseItemBehaviour {

	@Override
	protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3 facing) {
		double x = pos.getX() + facing.x * .7 + .5;
		double y = pos.getY() + facing.y * .7 + .5;
		double z = pos.getZ() + facing.z * .7 + .5;
		Projectile ProjectileEntity = this.getProjectileEntity(context.world, x, y, z, itemStack.copy());
		if (ProjectileEntity == null)
			return itemStack;
		Vec3 effectiveMovementVec = facing.scale(getProjectileVelocity()).add(context.motion);
		ProjectileEntity.shoot(effectiveMovementVec.x, effectiveMovementVec.y, effectiveMovementVec.z, (float) effectiveMovementVec.length(), this.getProjectileInaccuracy());
		context.world.addFreshEntity(ProjectileEntity);
		itemStack.shrink(1);
		return itemStack;
	}

	@Override
	protected void playDispenseSound(LevelAccessor world, BlockPos pos) {
		world.levelEvent(1002, pos, 0);
	}

	@Nullable
	protected abstract Projectile getProjectileEntity(Level world, double x, double y, double z, ItemStack itemStack);

	protected float getProjectileInaccuracy() {
		return 6.0F;
	}

	protected float getProjectileVelocity() {
		return 1.1F;
	}

	public static MovedProjectileDispenserBehaviour of(AbstractProjectileDispenseBehavior vanillaBehaviour) {
		return new MovedProjectileDispenserBehaviour() {
			@Override
			protected Projectile getProjectileEntity(Level world, double x, double y, double z, ItemStack itemStack) {
				try {
					return (Projectile) MovedProjectileDispenserBehaviour.getProjectileEntityLookup().invoke(vanillaBehaviour, world, new SimplePos(x, y, z) , itemStack);
				} catch (Throwable ignored) {
				}
				return null;
			}

			@Override
			protected float getProjectileInaccuracy() {
				try {
					return (float) MovedProjectileDispenserBehaviour.getProjectileInaccuracyLookup().invoke(vanillaBehaviour);
				} catch (Throwable ignored) {
				}
				return super.getProjectileInaccuracy();
			}

			@Override
			protected float getProjectileVelocity() {
				try {
					return (float) MovedProjectileDispenserBehaviour.getProjectileVelocityLookup().invoke(vanillaBehaviour);
				} catch (Throwable ignored) {
				}
				return super.getProjectileVelocity();
			}
		};
	}

	private static Method getProjectileEntityLookup() {
		Method getProjectileEntity = MethodGetter.findMethod(AbstractProjectileDispenseBehavior.class, "getProjectileEntity", "method_12844", Level.class, Position.class, ItemStack.class); // getProjectile
		getProjectileEntity.setAccessible(true);
		return getProjectileEntity;
	}

	private static Method getProjectileInaccuracyLookup() {
		Method getProjectileInaccuracy = MethodGetter.findMethod(AbstractProjectileDispenseBehavior.class, "getProjectileInaccuracy", "method_12845"); // getUncertainty
		getProjectileInaccuracy.setAccessible(true);
		return getProjectileInaccuracy;
	}

	private static Method getProjectileVelocityLookup() {
		Method getProjectileVelocity = MethodGetter.findMethod(AbstractProjectileDispenseBehavior.class, "getProjectileVelocity", "method_12846"); // getPower
		getProjectileVelocity.setAccessible(true);
		return getProjectileVelocity;
	}
}
