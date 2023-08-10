package com.simibubi.create.content.contraptions.behaviour.dispenser;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.mixin.accessor.AbstractProjectileDispenseBehaviorAccessor;

import net.minecraft.core.BlockPos;
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
		Projectile projectile = this.getProjectileEntity(context.world, x, y, z, itemStack.copy());
		if (projectile == null)
			return itemStack;
		Vec3 effectiveMovementVec = facing.scale(getProjectileVelocity()).add(context.motion);
		projectile.shoot(effectiveMovementVec.x, effectiveMovementVec.y, effectiveMovementVec.z, (float) effectiveMovementVec.length(), this.getProjectileInaccuracy());
		context.world.addFreshEntity(projectile);
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
		AbstractProjectileDispenseBehaviorAccessor accessor = (AbstractProjectileDispenseBehaviorAccessor) vanillaBehaviour;
		return new MovedProjectileDispenserBehaviour() {
			@Override
			protected Projectile getProjectileEntity(Level world, double x, double y, double z, ItemStack itemStack) {
				return accessor.create$callGetProjectile(world, new SimplePos(x, y, z), itemStack);
			}

			@Override
			protected float getProjectileInaccuracy() {
				return accessor.create$callGetUncertainty();
			}

			@Override
			protected float getProjectileVelocity() {
				return accessor.create$callGetPower();
			}
		};
	}
}
