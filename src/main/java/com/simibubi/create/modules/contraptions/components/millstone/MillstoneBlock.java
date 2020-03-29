package com.simibubi.create.modules.contraptions.components.millstone;

import com.simibubi.create.foundation.utility.AllShapes;
import com.simibubi.create.modules.contraptions.base.KineticBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class MillstoneBlock extends KineticBlock {

	public MillstoneBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new MillstoneTileEntity();
	}

	@Override
	protected boolean hasStaticPart() {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AllShapes.MILLSTONE;
	}

	@Override
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}
	
	@Override
	public ActionResultType onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		if (!player.getHeldItem(handIn).isEmpty())
			return ActionResultType.PASS;
		if (worldIn.getTileEntity(pos) == null)
			return ActionResultType.PASS;
		if (worldIn.isRemote)
			return ActionResultType.SUCCESS;

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof MillstoneTileEntity)) 
			return ActionResultType.PASS;
		MillstoneTileEntity millstone = (MillstoneTileEntity) tileEntity;
		
		IItemHandlerModifiable inv = millstone.outputInv;
		for (int slot = 0; slot < inv.getSlots(); slot++) {
			player.inventory.placeItemBackInInventory(worldIn, inv.getStackInSlot(slot));
			inv.setStackInSlot(slot, ItemStack.EMPTY);
		}
		millstone.markDirty();
		millstone.sendData();
		return ActionResultType.SUCCESS;
	}

	@Override
	public void onLanded(IBlockReader worldIn, Entity entityIn) {
		super.onLanded(worldIn, entityIn);

		if (entityIn.world.isRemote)
			return;
		if (!(entityIn instanceof ItemEntity))
			return;

		BlockPos pos = entityIn.getPosition();
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof MillstoneTileEntity)) {
			tileEntity = worldIn.getTileEntity(pos.down());
			if (!(tileEntity instanceof MillstoneTileEntity))
				return;
		}

		MillstoneTileEntity millstone = (MillstoneTileEntity) tileEntity;
		ItemEntity itemEntity = (ItemEntity) entityIn;
		LazyOptional<IItemHandler> capability = millstone.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!capability.isPresent())
			return;

		ItemStack remainder = capability.orElse(new ItemStackHandler()).insertItem(0, itemEntity.getItem(), false);
		if (remainder.isEmpty())
			itemEntity.remove();
		if (remainder.getCount() < itemEntity.getItem().getCount())
			itemEntity.setItem(remainder);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			TileEntity tileEntity = worldIn.getTileEntity(pos);
			if (!(tileEntity instanceof MillstoneTileEntity))
				return;
			MillstoneTileEntity te = (MillstoneTileEntity) tileEntity;
			for (int slot = 0; slot < te.inputInv.getSlots(); slot++) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
						te.inputInv.getStackInSlot(slot));
			}
			for (int slot = 0; slot < te.outputInv.getSlots(); slot++) {
				InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
						te.outputInv.getStackInSlot(slot));
			}

			worldIn.removeTileEntity(pos);
		}
	}

	@Override
	public boolean hasIntegratedCogwheel(IWorldReader world, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
	}

}
