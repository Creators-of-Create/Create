package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import net.minecraft.block.AbstractBlock.Properties;

public class AdjustableCrateBlock extends CrateBlock {

	public AdjustableCrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.ADJUSTABLE_CRATE.create();
	}

	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != state.getBlock() && state.hasTileEntity() && state.getValue(DOUBLE)
				&& state.getValue(FACING).getAxisDirection() == AxisDirection.POSITIVE) {
			TileEntity tileEntity = worldIn.getBlockEntity(pos);
			if (!(tileEntity instanceof AdjustableCrateTileEntity))
				return;

			AdjustableCrateTileEntity te = (AdjustableCrateTileEntity) tileEntity;
			AdjustableCrateTileEntity other = te.getOtherCrate();
			if (other == null)
				return;

			for (int slot = 0; slot < other.inventory.getSlots(); slot++) {
				te.inventory.setStackInSlot(slot, other.inventory.getStackInSlot(slot));
				other.inventory.setStackInSlot(slot, ItemStack.EMPTY);
			}
			te.allowedAmount = other.allowedAmount;
			other.invHandler.invalidate();
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {

		if (worldIn.isClientSide) {
			return ActionResultType.SUCCESS;
		} else {
			TileEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof AdjustableCrateTileEntity) {
				AdjustableCrateTileEntity fte = (AdjustableCrateTileEntity) te;
				fte = fte.getMainCrate();
				NetworkHooks.openGui((ServerPlayerEntity) player, fte, fte::sendToContainer);
			}
			return ActionResultType.SUCCESS;
		}
	}

	public static void splitCrate(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!AllBlocks.ADJUSTABLE_CRATE.has(state))
			return;
		if (!state.getValue(DOUBLE))
			return;
		TileEntity te = world.getBlockEntity(pos);
		if (!(te instanceof AdjustableCrateTileEntity))
			return;
		AdjustableCrateTileEntity crateTe = (AdjustableCrateTileEntity) te;
		crateTe.onSplit();
		world.setBlockAndUpdate(pos, state.setValue(DOUBLE, false));
		world.setBlockAndUpdate(crateTe.getOtherCrate().getBlockPos(), state.setValue(DOUBLE, false));
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!(worldIn.getBlockEntity(pos) instanceof AdjustableCrateTileEntity))
			return;

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			AdjustableCrateTileEntity te = (AdjustableCrateTileEntity) worldIn.getBlockEntity(pos);
			if (!isMoving)
				te.onDestroyed();
			worldIn.removeBlockEntity(pos);
		}

	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof AdjustableCrateTileEntity) {
			AdjustableCrateTileEntity flexcrateTileEntity = ((AdjustableCrateTileEntity) te).getMainCrate();
			return ItemHelper.calcRedstoneFromInventory(flexcrateTileEntity.inventory);
		}
		return 0;
	}

}
