package com.simibubi.create.modules.logistics.block.funnel;

import java.util.List;

import com.simibubi.create.modules.contraptions.components.contraptions.MovementBehaviour;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelMovementBehaviour extends MovementBehaviour {

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);

		World world = context.world;
		List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(pos));
		ItemStack filter = getFilter(context);

		for (ItemEntity item : items) {
			ItemStack toInsert = item.getItem();
			if (!filter.isEmpty() && !FilterItem.test(context.world, toInsert, filter))
				continue;
			ItemStack remainder = ItemHandlerHelper.insertItemStacked(context.contraption.inventory, toInsert, false);
			if (remainder.getCount() == toInsert.getCount())
				continue;
			if (remainder.isEmpty()) {
				item.setItem(ItemStack.EMPTY);
				item.remove();
				continue;
			}

			item.setItem(remainder);
		}

	}

	private ItemStack getFilter(MovementContext context) {
		return ItemStack.read(context.tileData.getCompound("Filter"));
	}

}
