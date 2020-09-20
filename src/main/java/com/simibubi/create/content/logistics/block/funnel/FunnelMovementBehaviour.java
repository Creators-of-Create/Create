package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.logistics.item.filter.FilterItem;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public class FunnelMovementBehaviour extends MovementBehaviour {

	private final boolean hasFilter;

	public static FunnelMovementBehaviour andesite() {
		return new FunnelMovementBehaviour(false);
	}

	public static FunnelMovementBehaviour brass() {
		return new FunnelMovementBehaviour(true);
	}

	private FunnelMovementBehaviour(boolean hasFilter) {
		this.hasFilter = hasFilter;
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(FunnelBlock.getFunnelFacing(context.state)
			.getDirectionVec()).scale(.65);
	}

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
		return hasFilter ? ItemStack.read(context.tileData.getCompound("Filter")) : ItemStack.EMPTY;
	}

}
