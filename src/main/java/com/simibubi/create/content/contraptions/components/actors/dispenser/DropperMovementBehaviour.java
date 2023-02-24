package com.simibubi.create.content.contraptions.components.actors.dispenser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DropperMovementBehaviour implements MovementBehaviour {
	protected static final MovedDefaultDispenseItemBehaviour DEFAULT_BEHAVIOUR =
		new MovedDefaultDispenseItemBehaviour();
	private static final Random RNG = new Random();

	protected void activate(MovementContext context, BlockPos pos) {
		DispenseItemLocation location = getDispenseLocation(context);
		if (location.isEmpty()) {
			context.world.levelEvent(1001, pos, 0);
		} else {
			setItemStackAt(location, DEFAULT_BEHAVIOUR.dispense(getItemStackAt(location, context), context, pos),
				context);
		}
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		if (context.world.isClientSide)
			return;
		collectItems(context);
		activate(context, pos);
	}

	private void collectItems(MovementContext context) {
		getStacks(context).stream()
			.filter(itemStack -> !itemStack.isEmpty() && itemStack.getItem() != Items.AIR
				&& itemStack.getMaxStackSize() > itemStack.getCount())
			.forEach(itemStack -> itemStack.grow(ItemHelper
				.extract(context.contraption.getSharedInventory(), itemStack::sameItem,
					ItemHelper.ExtractionCountMode.UPTO, itemStack.getMaxStackSize() - itemStack.getCount(), false)
				.getCount()));
	}

	private void updateTemporaryData(MovementContext context) {
		if (!(context.temporaryData instanceof NonNullList) && context.world != null) {
			NonNullList<ItemStack> stacks = NonNullList.withSize(getInvSize(), ItemStack.EMPTY);
			ContainerHelper.loadAllItems(context.blockEntityData, stacks);
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
		for (int slot = 0; slot < getInvSize(); slot++) {
			DispenseItemLocation location = new DispenseItemLocation(true, slot);
			ItemStack testStack = getItemStackAt(location, context);
			if (testStack == null || testStack.isEmpty())
				continue;
			if (testStack.getMaxStackSize() == 1) {
				location = new DispenseItemLocation(false, ItemHelper
					.findFirstMatchingSlotIndex(context.contraption.getSharedInventory(), testStack::sameItem));
				if (!getItemStackAt(location, context).isEmpty())
					useable.add(location);
			} else if (testStack.getCount() >= 2)
				useable.add(location);
		}
		return useable;
	}

	@Override
	public void writeExtraData(MovementContext context) {
		NonNullList<ItemStack> stacks = getStacks(context);
		if (stacks == null)
			return;
		ContainerHelper.saveAllItems(context.blockEntityData, stacks);
	}

	@Override
	public void stopMoving(MovementContext context) {
		MovementBehaviour.super.stopMoving(context);
		writeExtraData(context);
	}

	protected DispenseItemLocation getDispenseLocation(MovementContext context) {
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
			return context.contraption.getSharedInventory()
				.getStackInSlot(location.getSlot());
		}
	}

	protected void setItemStackAt(DispenseItemLocation location, ItemStack stack, MovementContext context) {
		if (location.isInternal()) {
			getStacks(context).set(location.getSlot(), stack);
		} else {
			context.contraption.getSharedInventory()
				.setStackInSlot(location.getSlot(), stack);
		}
	}

	private static int getInvSize() {
		return 9;
	}
}
