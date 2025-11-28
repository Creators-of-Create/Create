package com.simibubi.create.content.processing.basin;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class BasinMovementBehaviour implements MovementBehaviour {
	@Override
	public void tick(MovementContext context) {
		BasinMountedItemStorage storage = getMountedItemStorage(context);
		if(storage == null) return;

		int newTimesChanged = storage.getTimesChanged();
		if (getLastTimesChanged(context) != newTimesChanged) {
			Vec3 facingVec = context.rotation.apply(Vec3.atLowerCornerOf(Direction.UP.getNormal()));
			facingVec.normalize();
			if (Direction.getNearest(facingVec.x, facingVec.y, facingVec.z) == Direction.DOWN)
				dump(context, storage, facingVec);
		}
	}

	private void dump(MovementContext context, BasinMountedItemStorage storage, Vec3 facingVec) {

		for (int i = 0; i < storage.getSlots(); i++) {
			if (storage.getStackInSlot(i).isEmpty())
				continue;

			ItemEntity itemEntity = new ItemEntity(context.world, context.position.x, context.position.y, context.position.z, storage.getStackInSlot(i));
			itemEntity.setDeltaMovement(facingVec.scale(.05));
			context.world.addFreshEntity(itemEntity);

			storage.setStackInSlot(i, ItemStack.EMPTY);
		}

		context.temporaryData = storage.getTimesChanged();

		// FIXME: Why are we setting client-side data here?
//		if (context.contraption.entity.level().isClientSide) {
//			BlockEntity blockEntity = context.contraption.getBlockEntityClientSide(context.localPos);
//			if (blockEntity instanceof BasinBlockEntity)
//				((BasinBlockEntity) blockEntity).readOnlyItems(context.blockEntityData, context.world.registryAccess());
//		}
	}
}
