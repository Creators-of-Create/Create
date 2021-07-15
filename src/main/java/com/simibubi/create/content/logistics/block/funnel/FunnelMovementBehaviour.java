package com.simibubi.create.content.logistics.block.funnel;

import java.util.List;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
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
	public Vector3d getActiveAreaOffset(MovementContext context) {
		Direction facing = FunnelBlock.getFunnelFacing(context.state);
		Vector3d vec = Vector3d.atLowerCornerOf(facing.getNormal());
		if (facing != Direction.UP)
			return vec.scale(context.state.getValue(FunnelBlock.EXTRACTING) ? .15 : .65);

		return vec.scale(.65);
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);

		if (context.state.getValue(FunnelBlock.EXTRACTING))
			extract(context, pos);
		else
			succ(context, pos);


	}

	private void extract(MovementContext context, BlockPos pos) {
		World world = context.world;

		Vector3d entityPos = context.position;
		if (context.state.getValue(FunnelBlock.FACING) != Direction.DOWN)
			entityPos = entityPos.add(0, -.5f, 0);

		if (!world.getBlockState(pos).getCollisionShape(world, pos).isEmpty())
			return;//only drop items if the target block is a empty space

		if (!world.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(new BlockPos(entityPos))).isEmpty())
			return;//don't drop items if there already are any in the target block space

		ItemStack filter = getFilter(context);
		int filterAmount = context.tileData.getInt("FilterAmount");
		if (filterAmount <= 0)
			filterAmount = hasFilter ? AllConfigs.SERVER.logistics.defaultExtractionLimit.get() : 1;

		ItemStack extract = ItemHelper.extract(
				context.contraption.inventory,
				s -> FilterItem.test(world, s, filter),
				ItemHelper.ExtractionCountMode.UPTO,
				filterAmount,
				false);

		if (extract.isEmpty())
			return;

		if (world.isClientSide)
			return;



		ItemEntity entity = new ItemEntity(world, entityPos.x, entityPos.y, entityPos.z, extract);
		entity.setDeltaMovement(Vector3d.ZERO);
		entity.setPickUpDelay(5);
		world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.BLOCKS, 1/16f, .1f);
		world.addFreshEntity(entity);
	}

	private void succ(MovementContext context, BlockPos pos) {
		World world = context.world;
		List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(pos));
		ItemStack filter = getFilter(context);

		for (ItemEntity item : items) {
			if (!item.isAlive())
				continue;
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
		return hasFilter ? ItemStack.of(context.tileData.getCompound("Filter")) : ItemStack.EMPTY;
	}

}
