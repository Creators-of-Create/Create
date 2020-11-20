package com.simibubi.create.content.contraptions.processing;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.ItemStackHandler;

public class BasinMovementBehaviour extends MovementBehaviour {
	public Map<String, ItemStackHandler> getOrReadInventory(MovementContext context) {
		Map<String, ItemStackHandler> map = new HashMap<>();
		map.put("InputItems", new ItemStackHandler(9));
		map.put("OutputItems", new ItemStackHandler(8));
		map.forEach((s, h) -> h.deserializeNBT(context.tileData.getCompound(s)));
		return map;
	}

	@Override
	public boolean hasSpecialMovementRenderer() {
		return false;
	}

	@Override
	public void tick(MovementContext context) {
		super.tick(context);
		if (context.temporaryData == null || (boolean) context.temporaryData) {
			Vector3d facingVec = context.rotation.apply(Vector3d.of(Direction.UP.getDirectionVec()));
			facingVec.normalize();
			if (Direction.getFacingFromVector(facingVec.x, facingVec.y, facingVec.z) == Direction.DOWN)
				dump(context, facingVec);
		}
	}

	private void dump(MovementContext context, Vector3d facingVec) {
		getOrReadInventory(context).forEach((key, itemStackHandler) -> {
			for (int i = 0; i < itemStackHandler.getSlots(); i++) {
				if (itemStackHandler.getStackInSlot(i)
					.isEmpty())
					continue;
				ItemEntity itemEntity = new ItemEntity(context.world, context.position.x, context.position.y,
					context.position.z, itemStackHandler.getStackInSlot(i));
				itemEntity.setMotion(facingVec.scale(.05));
				context.world.addEntity(itemEntity);
				itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
			}
			context.tileData.put(key, itemStackHandler.serializeNBT());
		});
		context.contraption.renderedTileEntities.stream()
			.filter(te -> te.getPos()
				.equals(context.localPos) && te instanceof BasinTileEntity)
			.forEach(te -> ((BasinTileEntity) te).readOnlyItems(context.tileData));
		context.temporaryData = false; // did already dump, so can't any more
	}
}
