package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class MovedProjectileDispenserBehaviour extends MovedDefaultDispenseItemBehaviour {
	@Override
	protected ItemStack dispenseStack(ItemStack itemStack, MovementContext context, BlockPos pos, Vec3d facing) {
		double x = pos.getX() + facing.x * .7 + .5;
		double y = pos.getY() + facing.y * .7 + .5;
		double z = pos.getZ() + facing.z * .7 + .5;
		IProjectile iprojectile = this.getProjectileEntity(context.world, x, y, z, itemStack);
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

	protected abstract IProjectile getProjectileEntity(World world, double x, double y, double z, ItemStack itemStack);

	protected float getProjectileInaccuracy() {
		return 6.0F;
	}

	protected float getProjectileVelocity() {
		return 1.1F;
	}
}
