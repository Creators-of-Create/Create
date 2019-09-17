package com.simibubi.create.modules.logistics.block;

import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

// Its like delegation but better!
public interface IExtractor extends ITickableTileEntity, IInventoryManipulator {

	public enum State {
		WAITING_FOR_INVENTORY, WAITING_FOR_ENTITY, RUNNING, ON_COOLDOWN, LOCKED;
	}

	public State getState();

	public void setState(State state);

	public int tickCooldown();

	@Override
	default void tick() {
		if (isFrozen())
			return;

		State state = getState();

		if (state == State.LOCKED)
			return;

		if (state == State.ON_COOLDOWN) {
			int cooldown = tickCooldown();
			if (cooldown <= 0) {
				setState(State.RUNNING);
				if (!getInventory().isPresent())
					findNewInventory();
			}
			return;
		}

		boolean hasSpace = hasSpaceForExtracting();
		boolean hasInventory = getInventory().isPresent();
		ItemStack toExtract = ItemStack.EMPTY;

		if (hasSpace && hasInventory) {
			toExtract = extract(true);

			ItemStack filterItem = (this instanceof IHaveFilter) ? ((IHaveFilter) this).getFilter() : ItemStack.EMPTY;
			if (!filterItem.isEmpty() && !ItemStack.areItemsEqual(toExtract, filterItem))
				toExtract = ItemStack.EMPTY;
		}

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
		if (isFrozen())
			return;

		boolean hasSpace = hasSpaceForExtracting();
		boolean hasInventory = getInventory().isPresent();
		ItemStack toExtract = ItemStack.EMPTY;

		if (hasSpace && hasInventory) {
			toExtract = extract(true);
			ItemStack filterItem = (this instanceof IHaveFilter) ? ((IHaveFilter) this).getFilter() : ItemStack.EMPTY;
			if (!filterItem.isEmpty() && !ItemStack.areItemsEqual(toExtract, filterItem))
				toExtract = ItemStack.EMPTY;
		}

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
		ItemStack filterItem = (this instanceof IHaveFilter) ? ((IHaveFilter) this).getFilter() : ItemStack.EMPTY;
		int extractionCount = filterItem.isEmpty() ? CreateConfig.parameters.extractorAmount.get()
				: filterItem.getCount();
		boolean checkHasEnoughItems = !filterItem.isEmpty();
		boolean hasEnoughItems = !checkHasEnoughItems;

		Extraction: do {
			extracting = ItemStack.EMPTY;

			for (int slot = 0; slot < inv.getSlots(); slot++) {
				ItemStack stack = inv.extractItem(slot, extractionCount - extracting.getCount(), true);
				ItemStack compare = stack.copy();

				compare.setCount(filterItem.getCount());
				if (!filterItem.isEmpty() && !filterItem.equals(compare, false))
					continue;

				compare.setCount(extracting.getCount());
				if (!extracting.isEmpty() && !extracting.equals(compare, false))
					continue;

				if (extracting.isEmpty())
					extracting = stack.copy();
				else
					extracting.grow(stack.getCount());

				if (!simulate && hasEnoughItems)
					inv.extractItem(slot, stack.getCount(), false);

				if (extracting.getCount() >= extractionCount) {
					if (checkHasEnoughItems) {
						hasEnoughItems = true;
						checkHasEnoughItems = false;
						continue Extraction;
					} else {
						break Extraction;
					}
				}
			}

			if (checkHasEnoughItems)
				checkHasEnoughItems = false;
			else
				break Extraction;
		} while (true);

		if (!simulate && hasEnoughItems) {
			World world = getWorld();
			Vec3d pos = VecHelper.getCenterOf(getPos()).add(0, -0.5f, 0);
			ItemEntity entityIn = new ItemEntity(world, pos.x, pos.y, pos.z, extracting);
			entityIn.setMotion(Vec3d.ZERO);
			world.addEntity(entityIn);
			world.playSound(null, getPos(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .125f, .1f);
		}

		return extracting;
	}

	public static boolean isFrozen() {
		return CreateConfig.parameters.freezeExtractors.get();
	}

}
