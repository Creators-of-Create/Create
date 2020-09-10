package com.simibubi.create.content.contraptions.components.actors.dispenser;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropperMovementBehaviour extends MovementBehaviour {
	protected static final MovedDefaultDispenseItemBehaviour defaultBehaviour = new MovedDefaultDispenseItemBehaviour();
	private static final Random RNG = new Random();

	protected void activate(MovementContext context, BlockPos pos) {
		DispenseItemLocation location = getDispenseStack(context);
		if (location.isEmpty()) {
			context.world.playEvent(1001, pos, 0);
		} else {
			setItemStackAt(location, defaultBehaviour.dispense(getItemStackAt(location, context), context, pos), context);
		}
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.isRemote)
			return;
		collectItems(context);
		activate(context, pos);
	}

	private void collectItems(MovementContext context) {
		getStacks(context).stream().filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != Items.AIR && itemStack.getMaxStackSize() > itemStack.getCount()).forEach(itemStack -> itemStack.grow(
			ItemHelper.extract(context.contraption.inventory, itemStack::isItemEqual, ItemHelper.ExtractionCountMode.UPTO, itemStack.getMaxStackSize() - itemStack.getCount(), false).getCount()));
	}

	private void updateTemporaryData(MovementContext context) {
		if (!(context.temporaryData instanceof NonNullList) && context.world instanceof ServerWorld) {
			NonNullList<ItemStack> stacks = NonNullList.withSize(getInvSize(), ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(context.tileData, stacks);
			context.temporaryData = stacks;
		}
	}

	@SuppressWarnings("unchecked")
	private NonNullList<ItemStack> getStacks(MovementContext context) {
		updateTemporaryData(context);
		return (NonNullList<ItemStack>) context.temporaryData;
	}

	private ArrayList<DispenseItemLocation> getUseableLocations(MovementContext context) {
		ArrayList<DispenseItemLocation> useable = new ArrayList<>();
		NonNullList<ItemStack> internalStacks = getStacks(context);
		for (int slot = 0; slot < getInvSize(); slot++) {
			DispenseItemLocation location = new DispenseItemLocation(true, slot);
			ItemStack testStack = getItemStackAt(location, context);
			if (testStack == null || testStack.isEmpty())
				continue;
			if (testStack.getMaxStackSize() == 1) {
				location = new DispenseItemLocation(false, ItemHelper.findFirstMatchingSlotIndex(context.contraption.inventory, testStack::isItemEqual));
				if (!getItemStackAt(location, context).isEmpty())
					useable.add(location);
			} else if (internalStacks.get(slot).getCount() >= 2)
				useable.add(location);
		}
		return useable;
	}

	@Override
	public void writeExtraData(MovementContext context) {
		NonNullList<ItemStack> stacks = getStacks(context);
		if (stacks == null)
			return;
		ItemStackHelper.saveAllItems(context.tileData, stacks);
	}

	@Override
	public void stopMoving(MovementContext context) {
		super.stopMoving(context);
		writeExtraData(context);
	}

	protected DispenseItemLocation getDispenseStack(MovementContext context) {
		int i = -1;
		int j = 1;
		List<DispenseItemLocation> useableLocations = getUseableLocations(context);
		for (int k = 0; k < useableLocations.size(); ++k) {
			if (RNG.nextInt(j++) == 0) {
				i = k;
			}
		}
		if (i < 0)
			return DispenseItemLocation.NONE;
		else
			return useableLocations.get(i);
	}

	protected ItemStack getItemStackAt(DispenseItemLocation location, MovementContext context) {
		if (location.isInternal()) {
			return getStacks(context).get(location.getSlot());
		} else {
			return context.contraption.inventory.getStackInSlot(location.getSlot());
		}
	}

	protected void setItemStackAt(DispenseItemLocation location, ItemStack stack, MovementContext context) {
		if (location.isInternal()) {
			getStacks(context).set(location.getSlot(), stack);
		} else {
			context.contraption.inventory.setStackInSlot(location.getSlot(), stack);
		}
	}

	private static int getInvSize() {
		return 9;
	}
}
