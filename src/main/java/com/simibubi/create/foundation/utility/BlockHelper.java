package com.simibubi.create.foundation.utility;

import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BlockHelper {

	public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
		int amountFound = 0;
		Item required = getRequiredItem(block).getItem();

		boolean needsTwo = block.has(BlockStateProperties.SLAB_TYPE)
				&& block.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;

		if (needsTwo)
			amount *= 2;

		{
			// Try held Item first
			int preferredSlot = player.inventory.currentItem;
			ItemStack itemstack = player.inventory.getStackInSlot(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setInventorySlotContents(preferredSlot,
						new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.inventory.getStackInSlot(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setInventorySlotContents(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.inventory.addItemStackToInventory(new ItemStack(required));
			amountFound /= 2;
		}

		return amountFound;
	}

	public static ItemStack getRequiredItem(BlockState state) {
		ItemStack itemStack = new ItemStack(state.getBlock());
		return itemStack;
	}

	public static void destroyBlock(World world, BlockPos pos, float effectChance) {
		destroyBlock(world, pos, effectChance, stack -> Block.spawnAsEntity(world, pos, stack));
	}

	public static void destroyBlock(World world, BlockPos pos, float effectChance,
			Consumer<ItemStack> droppedItemCallback) {
		FluidState ifluidstate = world.getFluidState(pos);
		BlockState state = world.getBlockState(pos);
		if (world.rand.nextFloat() < effectChance)
			world.playEvent(2001, pos, Block.getStateId(state));
		TileEntity tileentity = state.hasTileEntity() ? world.getTileEntity(pos) : null;

		if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS) && !world.restoringBlockSnapshots) {
			for (ItemStack itemStack : Block.getDrops(state, (ServerWorld) world, pos, tileentity))
				droppedItemCallback.accept(itemStack);
			state.spawnAdditionalDrops(world, pos, ItemStack.EMPTY);
		}

		world.setBlockState(pos, ifluidstate.getBlockState());
	}

}
