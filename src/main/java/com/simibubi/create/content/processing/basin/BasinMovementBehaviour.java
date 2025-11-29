package com.simibubi.create.content.processing.basin;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageWrapper;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;

import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.items.ItemHandlerHelper;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasinMovementBehaviour implements MovementBehaviour {
	@Override
	public void tick(MovementContext context) {
		BasinMountedItemStorage storage = getMountedItemStorage(context);
		if (storage == null) return;

		Vec3 facingVec = context.rotation.apply(Vec3.atLowerCornerOf(Direction.UP.getNormal())).normalize();
		Direction nearest = Direction.getNearest(facingVec.x, facingVec.y, facingVec.z);

		if (nearest == Direction.DOWN) {
			int newTimesChanged = storage.getTimesChanged();
			if (getLastTimesChanged(context) != newTimesChanged) {
				dump(context, storage, facingVec);
			}
		} else if(nearest == Direction.UP) {
			pickup(context, storage);
		}
	}

	private void pickup(MovementContext context, BasinMountedItemStorage storage) {
		Level world = context.world;

		Vec3 halfBlock = new Vec3(0.4, 0.4, 0.4);
		Vec3 posTop = context.position.add(halfBlock);
		Vec3 posBottom = context.position.subtract(halfBlock);

		List<Entity> items = world.getEntities((Entity) null, new AABB(posTop, posBottom),
			e -> e instanceof ItemEntity || e instanceof PackageEntity);

		for (Entity entity : items) {
			if (!entity.isAlive())
				continue;
			ItemStack toInsert = ItemHelper.fromItemEntity(entity);
			ItemStack remainder =
				ItemHandlerHelper.insertItemStacked(storage, toInsert, false);
			if (remainder.getCount() == toInsert.getCount())
				continue;
			if (remainder.isEmpty()) {
				entity.discard();
				continue;
			}

			if (entity instanceof ItemEntity item)
				item.setItem(remainder);
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
	}

	private int getLastTimesChanged(MovementContext context) {
		if(context.temporaryData != null) return (int)context.temporaryData;
		return 0;
	}

	private @Nullable BasinMountedItemStorage getMountedItemStorage(MovementContext context) {
		MountedItemStorageWrapper wrapper = context.contraption.getStorage().getMountedItems();
		MountedItemStorage storageHere = wrapper.storages.get(context.localPos);

		if(storageHere instanceof BasinMountedItemStorage basin) return basin;
		return null;
	}

	private @Nullable BasinMountedFluidStorage getMountedFluidStorage(MovementContext context) {
		MountedFluidStorageWrapper wrapper = context.contraption.getStorage().getFluids();
		MountedFluidStorage storageHere = wrapper.storages.get(context.localPos);

		if(storageHere instanceof BasinMountedFluidStorage basin) return basin;
		return null;
	}

	@Override
	public boolean disableBlockEntityRendering() {
		return true;
	}

	@Override
	public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockEntity blockEntity = context.contraption.getBlockEntityClientSide(context.localPos);
		if (blockEntity instanceof BasinBlockEntity) {
			BasinMountedItemStorage mountedItemStorage = getMountedItemStorage(context);
			if(mountedItemStorage == null) return;

			BasinMountedFluidStorage mountedFluidStorage = getMountedFluidStorage(context);

			BasinRenderer.renderInContraption(context, renderWorld, matrices, buffer,
				mountedItemStorage, mountedFluidStorage);
		}
	}
}
