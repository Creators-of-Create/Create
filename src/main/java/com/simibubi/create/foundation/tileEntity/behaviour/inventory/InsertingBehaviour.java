package com.simibubi.create.foundation.tileEntity.behaviour.inventory;

import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class InsertingBehaviour extends InventoryManagementBehaviour {

	public static BehaviourType<InsertingBehaviour> TYPE = new BehaviourType<>();

	public InsertingBehaviour(SmartTileEntity te, Supplier<List<Pair<BlockPos, Direction>>> attachments) {
		super(te, attachments);
	}

	public ItemStack insert(ItemStack stack, boolean simulate) {
		for (IItemHandler inv : getInventories()) {
			stack = ItemHandlerHelper.insertItemStacked(inv, stack, simulate);
			if (stack.isEmpty())
				break;
		}
		return stack;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

}
