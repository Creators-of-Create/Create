package com.simibubi.create.content.contraptions.processing;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import com.simibubi.create.lib.transfer.item.ItemStackHandler;

public class BasinMovementBehaviour extends MovementBehaviour {
	public Map<String, ItemStackHandler> getOrReadInventory(MovementContext context) {
		Map<String, ItemStackHandler> map = new HashMap<>();
		map.put("InputItems", new ItemStackHandler(9));
		map.put("OutputItems", new ItemStackHandler(8));
		map.forEach((s, h) -> h.deserializeNBT(context.tileData.getCompound(s)));
		return map;
	}

	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		super.tick(context);
		if (context.temporaryData == null || (boolean) context.temporaryData) {
			Vec3 facingVec = context.rotation.apply(Vec3.atLowerCornerOf(Direction.UP.getNormal()));
			facingVec.normalize();
			if (Direction.getNearest(facingVec.x, facingVec.y, facingVec.z) == Direction.DOWN)
				dump(context, facingVec);
		}
	}

	private void dump(MovementContext context, Vec3 facingVec) {
		getOrReadInventory(context).forEach((key, itemStackHandler) -> {
			for (int i = 0; i < itemStackHandler.getSlots(); i++) {
				if (itemStackHandler.getStackInSlot(i)
					.isEmpty())
					continue;
				ItemEntity itemEntity = new ItemEntity(context.world, context.position.x, context.position.y,
					context.position.z, itemStackHandler.getStackInSlot(i));
				itemEntity.setDeltaMovement(facingVec.scale(.05));
				context.world.addFreshEntity(itemEntity);
				itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
			}
			context.tileData.put(key, itemStackHandler.serializeNBT());
		});
		BlockEntity tileEntity = context.contraption.presentTileEntities.get(context.localPos);
		if (tileEntity instanceof BasinTileEntity)
			((BasinTileEntity) tileEntity).readOnlyItems(context.tileData);
		context.temporaryData = false; // did already dump, so can't any more
	}
}
