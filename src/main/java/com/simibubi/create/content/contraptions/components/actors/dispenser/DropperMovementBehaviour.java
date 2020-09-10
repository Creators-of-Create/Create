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
		ItemStack itemstack = getDispenseStack(context);
		if (itemstack.isEmpty()) {
			context.world.playEvent(1001, pos, 0);
		} else {
			defaultBehaviour.dispense(itemstack, context, pos);
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

	@SuppressWarnings("unchecked")
	private NonNullList<ItemStack> getStacks(MovementContext context) {
		if (!(context.temporaryData instanceof NonNullList) && context.world instanceof ServerWorld) {
			NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(context.tileData, stacks);
			context.temporaryData = stacks;
		}
		return (NonNullList<ItemStack>) context.temporaryData;
	}

	private ArrayList<ItemStack> getUseableStacks(MovementContext context) {
		ArrayList<ItemStack> useable = new ArrayList<>();
		for (ItemStack testStack : getStacks(context)) {
			if (testStack == null || testStack.isEmpty())
				continue;
			if (testStack.getMaxStackSize() == 1) {
				ItemStack stack = ItemHelper.findFirstMatch(context.contraption.inventory, testStack::isItemEqual);
				if (!stack.isEmpty())
					useable.add(stack);
			} else if (testStack.getCount() >= 2)
				useable.add(testStack);
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

	protected ItemStack getDispenseStack(MovementContext context) {
		int i = -1;
		int j = 1;
		List<ItemStack> stacks = getUseableStacks(context);
		for (int k = 0; k < stacks.size(); ++k) {
			if (RNG.nextInt(j++) == 0) {
				i = k;
			}
		}
		if (i < 0)
			return ItemStack.EMPTY;
		else
			return stacks.get(i);
	}
}
