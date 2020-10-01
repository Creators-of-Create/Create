package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasinMovementBehaviour extends MovementBehaviour {
	@SuppressWarnings("unchecked")
	public Map<String, ItemStackHandler> getOrReadInventory(MovementContext context) {
		if (!(context.temporaryData instanceof List)) {
			Map<String, ItemStackHandler> map =  new HashMap<>();
			map.put("InputItems", new ItemStackHandler(9));
			map.put("OutputItems", new ItemStackHandler(8));
			map.forEach((s, h) -> h.deserializeNBT(context.tileData.getCompound(s)));
			context.temporaryData = map;
		}
		return (Map<String, ItemStackHandler>) context.temporaryData;
	}

	@Override
	public void writeExtraData(MovementContext context) {
		super.writeExtraData(context);
		getOrReadInventory(context).forEach((s, h) -> context.tileData.put(s, h.serializeNBT()));
	}

	@Override
	public boolean hasSpecialMovementRenderer() {
		return false;
	}

	@Override
	public void tick(MovementContext context) {
		super.tick(context);
		Vec3d facingVec = VecHelper.rotate(new Vec3d(Direction.UP.getDirectionVec()), context.rotation.x, context.rotation.y, context.rotation.z);
		facingVec.normalize();
		if (Direction.getFacingFromVector(facingVec.x, facingVec.y, facingVec.z) == Direction.DOWN)
			dump(context, facingVec);
	}

	private void dump(MovementContext context, Vec3d facingVec) {
		getOrReadInventory(context).forEach((key, itemStackHandler) -> {
			for (int i = 0; i < itemStackHandler.getSlots(); i++) {
				if (itemStackHandler.getStackInSlot(i).isEmpty())
					continue;
				ItemEntity itemEntity = new ItemEntity(context.world, context.position.x, context.position.y, context.position.z, itemStackHandler.getStackInSlot(i));
				itemEntity.setMotion(facingVec.scale(.05));
				context.world.addEntity(itemEntity);
				itemStackHandler.setStackInSlot(i, ItemStack.EMPTY);
			}
			context.tileData.put(key, itemStackHandler.serializeNBT());
		});
		context.temporaryData = null;
		context.contraption.customRenderTEs.stream().filter(te -> te.getPos().equals(context.localPos)).forEach(te -> te.read(context.tileData));
	}
}
