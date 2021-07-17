package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.processing.EmptyingByBasin;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.tileEntity.ComparatorUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class ItemDrainBlock extends Block implements IWrenchable, ITE<ItemDrainTileEntity> {

	public ItemDrainBlock(Properties p_i48440_1_) {
		super(p_i48440_1_);
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
		BlockRayTraceResult hit) {
		ItemStack heldItem = player.getItemInHand(handIn);

		return onTileEntityUse(worldIn, pos, te -> {
			if (!heldItem.isEmpty()) {
				te.internalTank.allowInsertion();
				ActionResultType tryExchange = tryExchange(worldIn, player, handIn, heldItem, te);
				te.internalTank.forbidInsertion();
				if (tryExchange.consumesAction())
					return tryExchange;
			}

			ItemStack heldItemStack = te.getHeldItemStack();
			if (!worldIn.isClientSide && !heldItemStack.isEmpty()) {
				player.inventory.placeItemBackInInventory(worldIn, heldItemStack);
				te.heldItem = null;
				te.notifyUpdate();
			}
			return ActionResultType.SUCCESS;
		});
	}

	protected ActionResultType tryExchange(World worldIn, PlayerEntity player, Hand handIn, ItemStack heldItem,
		ItemDrainTileEntity te) {
		if (FluidHelper.tryEmptyItemIntoTE(worldIn, player, handIn, heldItem, te))
			return ActionResultType.SUCCESS;
		if (EmptyingByBasin.canItemBeEmptied(worldIn, heldItem))
			return ActionResultType.SUCCESS;
		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CASING_13PX.get(Direction.UP);
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.hasTileEntity() || state.getBlock() == newState.getBlock())
			return;
		withTileEntityDo(worldIn, pos, te -> {
			ItemStack heldItemStack = te.getHeldItemStack();
			if (!heldItemStack.isEmpty())
				InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), heldItemStack);
		});
		worldIn.removeBlockEntity(pos);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ITEM_DRAIN.create();
	}

	@Override
	public Class<ItemDrainTileEntity> getTileEntityClass() {
		return ItemDrainTileEntity.class;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		return ComparatorUtil.levelOfSmartFluidTank(worldIn, pos);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
