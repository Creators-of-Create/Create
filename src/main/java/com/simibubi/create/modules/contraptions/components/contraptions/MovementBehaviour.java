package com.simibubi.create.modules.contraptions.components.contraptions;

import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class MovementBehaviour {

	public boolean isActive(MovementContext context) {
		return true;
	}

	public void tick(MovementContext context) {
	}

	public void startMoving(MovementContext context) {
	}

	public void visitNewPosition(MovementContext context, BlockPos pos) {
	}

	public Vec3d getActiveAreaOffset(MovementContext context) {
		return Vec3d.ZERO;
	}

	public void dropItem(MovementContext context, ItemStack stack) {
		ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.inventory, stack, false);
		if (remainder.isEmpty())
			return;

		Vec3d vec = context.position;
		ItemEntity itemEntity = new ItemEntity(context.world, vec.x, vec.y, vec.z, remainder);
		itemEntity.setMotion(context.motion.add(0, 0.5f, 0).scale(context.world.rand.nextFloat() * .3f));
		context.world.addEntity(itemEntity);
	}

	@OnlyIn(value = Dist.CLIENT)
	public SuperByteBuffer renderInContraption(MovementContext context) {
		return null;
	}
	
	public void stopMoving(MovementContext context) {
		
	}

}
