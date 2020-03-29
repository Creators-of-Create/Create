package com.simibubi.create.modules.schematics.block;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.AllShapes;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class SchematicannonBlock extends Block {

	public SchematicannonBlock() {
		super(Properties.from(Blocks.DISPENSER));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SchematicannonTileEntity();
	}
	
	@Override
	public PushReaction getPushReaction(BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.SCHEMATICANNON_SHAPE;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
		((SchematicannonTileEntity) world.getTileEntity(pos)).findInventories();
		super.onNeighborChange(state, world, pos, neighbor);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {

		if (worldIn.isRemote) {
			return true;
		} else {
			SchematicannonTileEntity te = (SchematicannonTileEntity) worldIn.getTileEntity(pos);
			if (te != null)
				if (AllItems.BLUEPRINT.typeOf(player.getHeldItemMainhand())
						&& te.inventory.getStackInSlot(0).isEmpty()) {
					te.inventory.setStackInSlot(0, player.getHeldItemMainhand());
					player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
				}
			NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer);
			return true;
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.getTileEntity(pos) == null)
			return;

		SchematicannonTileEntity te = (SchematicannonTileEntity) worldIn.getTileEntity(pos);
		for (int slot = 0; slot < te.inventory.getSlots(); slot++) {
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					te.inventory.getStackInSlot(slot));
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}

	}

}
