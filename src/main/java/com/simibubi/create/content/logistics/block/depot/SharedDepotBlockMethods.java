package com.simibubi.create.content.logistics.block.depot;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

public class SharedDepotBlockMethods {

	protected static DepotBehaviour get(IBlockReader worldIn, BlockPos pos) {
		return TileEntityBehaviour.get(worldIn, pos, DepotBehaviour.TYPE);
	}

	public static ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult ray) {
		if (ray.getFace() != Direction.UP)
			return ActionResultType.PASS;
		if (world.isRemote)
			return ActionResultType.SUCCESS;

		DepotBehaviour behaviour = get(world, pos);
		if (behaviour == null)
			return ActionResultType.PASS;
		if (!behaviour.canAcceptItems.get())
			return ActionResultType.SUCCESS;

		ItemStack heldItem = player.getHeldItem(hand);
		boolean wasEmptyHanded = heldItem.isEmpty();
		boolean shouldntPlaceItem = AllBlocks.MECHANICAL_ARM.isIn(heldItem);

		ItemStack mainItemStack = behaviour.getHeldItemStack();
		if (!mainItemStack.isEmpty()) {
			player.inventory.placeItemBackInInventory(world, mainItemStack);
			behaviour.removeHeldItem();
		}
		ItemStackHandler outputs = behaviour.processingOutputBuffer;
		for (int i = 0; i < outputs.getSlots(); i++)
			player.inventory.placeItemBackInInventory(world, outputs.extractItem(i, 64, false));

		if (!wasEmptyHanded && !shouldntPlaceItem) {
			TransportedItemStack transported = new TransportedItemStack(heldItem);
			transported.insertedFrom = player.getHorizontalFacing();
			transported.prevBeltPosition = .25f;
			transported.beltPosition = .25f;
			behaviour.setHeldItem(transported);
			player.setHeldItem(hand, ItemStack.EMPTY);
			AllSoundEvents.DEPOT_SLIDE.playOnServer(world, pos);
		}

		behaviour.tileEntity.notifyUpdate();
		return ActionResultType.SUCCESS;
	}

	public static void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState,
		boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;
		DepotBehaviour behaviour = get(worldIn, pos);
		if (behaviour == null)
			return;
		ItemHelper.dropContents(worldIn, pos, behaviour.processingOutputBuffer);
		for (TransportedItemStack transportedItemStack : behaviour.incoming)
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), transportedItemStack.stack);
		if (!behaviour.getHeldItemStack()
			.isEmpty())
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), behaviour.getHeldItemStack());
		worldIn.removeTileEntity(pos);
	}

	public static void onLanded(IBlockReader worldIn, Entity entityIn) {
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		if (entityIn.world.isRemote)
			return;

		ItemEntity itemEntity = (ItemEntity) entityIn;
		DirectBeltInputBehaviour inputBehaviour =
			TileEntityBehaviour.get(worldIn, entityIn.getBlockPos(), DirectBeltInputBehaviour.TYPE);
		if (inputBehaviour == null)
			return;
		ItemStack remainder = inputBehaviour.handleInsertion(itemEntity.getItem(), Direction.DOWN, false);
		itemEntity.setItem(remainder);
		if (remainder.isEmpty())
			itemEntity.remove();
	}

	public static int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		DepotBehaviour depotBehaviour = get(worldIn, pos);
		if (depotBehaviour == null)
			return 0;
		return ItemHelper.calcRedstoneFromInventory(depotBehaviour.itemHandler);
	}

}
