package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

// Its like delegation but better!
public interface IExtractor extends ITickableTileEntity, IInventoryManipulator {

	public static final int EXTRACTOR_COOLDOWN = 20;
	public static final int EXTRACTION_COUNT = 16;

	public enum State {
		WAITING_FOR_INVENTORY, WAITING_FOR_ENTITY, RUNNING, ON_COOLDOWN, LOCKED;
	}

	public State getState();

	public void setState(State state);

	public int tickCooldown();

	@Override
	default void tick() {
		State state = getState();

		if (state == State.LOCKED)
			return;

		if (state == State.ON_COOLDOWN) {
			int cooldown = tickCooldown();
			if (cooldown <= 0)
				setState(State.RUNNING);
			return;
		}

		boolean hasSpace = hasSpaceForExtracting();
		boolean hasInventory = getInventory().isPresent();
		ItemStack toExtract = ItemStack.EMPTY;

		if (hasSpace && hasInventory)
			toExtract = extract(true);

		if (state == State.WAITING_FOR_ENTITY) {
			if (hasSpace)
				setState(State.RUNNING);
		}

		if (state == State.RUNNING) {
			if (!hasSpace) {
				setState(State.WAITING_FOR_ENTITY);
				return;
			}
			if (!hasInventory || toExtract.isEmpty()) {
				setState(State.WAITING_FOR_INVENTORY);
				return;
			}

			extract(false);
			setState(State.ON_COOLDOWN);

			return;
		}

	}

	public default void setLocked(boolean locked) {
		setState(locked ? State.LOCKED : State.ON_COOLDOWN);
	}

	public default void neighborChanged() {
		boolean hasSpace = hasSpaceForExtracting();
		boolean hasInventory = getInventory().isPresent();
		ItemStack toExtract = ItemStack.EMPTY;

		if (hasSpace && hasInventory)
			toExtract = extract(true);

		if (getState() == State.WAITING_FOR_INVENTORY) {
			if (!hasInventory) {
				if (findNewInventory()) {
					setState(State.RUNNING);
				}
			}
			if (!toExtract.isEmpty())
				setState(State.RUNNING);
			return;
		}
	}

	default boolean hasSpaceForExtracting() {
		return getWorld().getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(getPos())).isEmpty();
	}

	default ItemStack extract(boolean simulate) {
		IItemHandler inv = getInventory().orElse(null);
		ItemStack extracting = ItemStack.EMPTY;

		for (int slot = 0; slot < inv.getSlots(); slot++) {
			ItemStack stack = inv.extractItem(slot, EXTRACTION_COUNT - extracting.getCount(), true);
			ItemStack compare = stack.copy();
			compare.setCount(extracting.getCount());
			if (!extracting.isEmpty() && !extracting.equals(compare, false))
				continue;

			if (extracting.isEmpty())
				extracting = stack.copy();
			else
				extracting.grow(stack.getCount());

			if (!simulate)
				inv.extractItem(slot, stack.getCount(), false);
			if (extracting.getCount() >= EXTRACTION_COUNT)
				break;
		}

		if (!simulate) {
			World world = getWorld();
			Vec3d pos = VecHelper.getCenterOf(getPos()).add(0, -0.5f, 0);
			ItemEntity entityIn = new ItemEntity(world, pos.x, pos.y, pos.z, extracting);
			entityIn.setMotion(Vec3d.ZERO);
			world.addEntity(entityIn);
		}

		return extracting;
	}

}
