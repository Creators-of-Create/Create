package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.lib.utility.NetworkUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AdjustableCrateBlock extends CrateBlock implements ITE<AdjustableCrateTileEntity> {

	public AdjustableCrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (oldState.getBlock() != state.getBlock() && state.hasBlockEntity() && state.getValue(DOUBLE)
				&& state.getValue(FACING).getAxisDirection() == AxisDirection.POSITIVE) {
			BlockEntity tileEntity = worldIn.getBlockEntity(pos);
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {

		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity te = worldIn.getBlockEntity(pos);
			if (te instanceof AdjustableCrateTileEntity) {
				AdjustableCrateTileEntity fte = (AdjustableCrateTileEntity) te;
				fte = fte.getMainCrate();
				NetworkUtil.openGui((ServerPlayer) player, fte, fte::sendToContainer);
			}
			return InteractionResult.SUCCESS;
		}
	}

	public static void splitCrate(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (!AllBlocks.ADJUSTABLE_CRATE.has(state))
			return;
		if (!state.getValue(DOUBLE))
			return;
		BlockEntity te = world.getBlockEntity(pos);
		if (!(te instanceof AdjustableCrateTileEntity))
			return;
		AdjustableCrateTileEntity crateTe = (AdjustableCrateTileEntity) te;
		crateTe.onSplit();
		world.setBlockAndUpdate(pos, state.setValue(DOUBLE, false));
		world.setBlockAndUpdate(crateTe.getOtherCrate().getBlockPos(), state.setValue(DOUBLE, false));
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!(worldIn.getBlockEntity(pos) instanceof AdjustableCrateTileEntity))
			return;

		if (state.hasBlockEntity() && state.getBlock() != newState.getBlock()) {
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
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof AdjustableCrateTileEntity) {
			AdjustableCrateTileEntity flexcrateTileEntity = ((AdjustableCrateTileEntity) te).getMainCrate();
			return ItemHelper.calcRedstoneFromInventory(flexcrateTileEntity.inventory);
		}
		return 0;
	}

	@Override
	public Class<AdjustableCrateTileEntity> getTileEntityClass() {
		return AdjustableCrateTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends AdjustableCrateTileEntity> getTileEntityType() {
		return AllTileEntities.ADJUSTABLE_CRATE.get();
	}

}
