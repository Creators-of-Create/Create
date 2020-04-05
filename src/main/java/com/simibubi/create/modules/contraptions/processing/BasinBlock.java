package com.simibubi.create.modules.contraptions.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

public class BasinBlock extends Block implements ITE<BasinTileEntity> {

	public BasinBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new BasinTileEntity();
	}

	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.getHeldItem(handIn).isEmpty())
			return false;

		try {
			BasinTileEntity te = getTileEntity(worldIn, pos);
			IItemHandlerModifiable inv = te.inventory.orElse(new ItemStackHandler(1));
			for (int slot = 0; slot < inv.getSlots(); slot++) {
				player.inventory.placeItemBackInInventory(worldIn, inv.getStackInSlot(slot));
				inv.setStackInSlot(slot, ItemStack.EMPTY);
			}
			te.onEmptied();
		} catch (TileEntityException e) {}

		return true;
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);
		if (!AllBlocks.BASIN.typeOf(worldIn.getBlockState(entityIn.getPosition())))
			return;
		if (!(entityIn instanceof ItemEntity))
			return;
		if (!entityIn.isAlive())
			return;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		withTileEntityDo(worldIn, entityIn.getPosition(), te -> {
			ItemStack insertItem = ItemHandlerHelper.insertItem(te.inputInventory, itemEntity.getItem().copy(), false);

			if (insertItem.isEmpty()) {
				itemEntity.remove();
				return;
			}

			itemEntity.setItem(insertItem);
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.BASIN_BLOCK_SHAPE;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock()) {
			return;
		}

		withTileEntityDo(worldIn, pos, te -> {
			ItemHelper.dropContents(worldIn, pos, te.inputInventory);
			ItemHelper.dropContents(worldIn, pos, te.outputInventory);
		});
		worldIn.removeTileEntity(pos);
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		try {
			return ItemHelper.calcRedstoneFromInventory(getTileEntity(worldIn, pos).inputInventory);
		} catch (TileEntityException e) {}
		return 0;
	}

	@Override
	public Class<BasinTileEntity> getTileEntityClass() {
		return BasinTileEntity.class;
	}

}
