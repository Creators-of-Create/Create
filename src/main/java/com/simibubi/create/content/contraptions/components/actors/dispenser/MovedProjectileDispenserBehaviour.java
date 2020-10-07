package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public abstract class MovedProjectileDispenserBehaviour extends MovedDefaultDispenseItemBehaviour {

	@Override
	protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
		double x = pos.getX() + facing.x * .7 + .5;
		double y = pos.getY() + facing.y * .7 + .5;
		double z = pos.getZ() + facing.z * .7 + .5;
		IProjectile iprojectile = this.getProjectileEntity(context.world, x, y, z, itemStack.copy());
		if (iprojectile == null)
			return itemStack;
		Vec3d effectiveMovementVec = facing.scale(getProjectileVelocity()).add(context.motion);
		iprojectile.shoot(effectiveMovementVec.x, effectiveMovementVec.y, effectiveMovementVec.z, (float) effectiveMovementVec.length(), this.getProjectileInaccuracy());
		context.world.addEntity((Entity) iprojectile);
		itemStack.shrink(1);
		return itemStack;
	}

	@Override
	protected void playDispenseSound(IWorld world, BlockPos pos) {
		world.playEvent(1002, pos, 0);
	}

	@Nullable
	protected abstract IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack);

	protected float getProjectileInaccuracy() {
		return 6.0F;
	}

	protected float getProjectileVelocity() {
		return 1.1F;
	}

	public static MovedProjectileDispenserBehaviour of(ProjectileDispenseBehavior vanillaBehaviour) {
		return new MovedProjectileDispenserBehaviour() {
			@Override
			protected IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack) {
				try {
					return (IProjectile) MovedProjectileDispenserBehaviour.getProjectileEntityLookup().invoke(vanillaBehaviour, world, new SimplePos(x, y, z) , itemStack);
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
		Method getProjectileEntity = ObfuscationReflectionHelper.findMethod(ProjectileDispenseBehavior.class, "func_82499_a", World.class, IPosition.class, ItemStack.class);
		getProjectileEntity.setAccessible(true);
		return getProjectileEntity;
	}

	private static Method getProjectileInaccuracyLookup() {
		Method getProjectileInaccuracy = ObfuscationReflectionHelper.findMethod(ProjectileDispenseBehavior.class, "func_82498_a");
		getProjectileInaccuracy.setAccessible(true);
		return getProjectileInaccuracy;
	}

	private static Method getProjectileVelocityLookup() {
		Method getProjectileVelocity = ObfuscationReflectionHelper.findMethod(ProjectileDispenseBehavior.class, "func_82500_b");
		getProjectileVelocity.setAccessible(true);
		return getProjectileVelocity;
	}
}
