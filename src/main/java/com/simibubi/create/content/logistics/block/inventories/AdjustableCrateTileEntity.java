package com.simibubi.create.content.logistics.block.inventories;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import com.simibubi.create.lib.utility.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class AdjustableCrateTileEntity extends CrateTileEntity implements MenuProvider {

	public class Inv extends ItemStackHandler {
		public Inv() {
			super(32);
		}

		@Override
		public int getSlotLimit(int slot) {
			if (slot < allowedAmount / 64)
				return super.getSlotLimit(slot);
			else if (slot == allowedAmount / 64)
				return allowedAmount % 64;
			return 0;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot > allowedAmount / 64)
				return false;
			return super.isItemValid(slot, stack);
		}

		@Override
		protected void onContentsChanged(int slot) {
			super.onContentsChanged(slot);
			setChanged();

			itemCount = 0;
			for (int i = 0; i < getSlots(); i++) {
				itemCount += getStackInSlot(i).getCount();
			}
		}
	}

	public Inv inventory;
	public int allowedAmount;
	public int itemCount;
	protected LazyOptional<IItemHandler> invHandler;

	public AdjustableCrateTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		allowedAmount = 512;
		itemCount = 10;
		inventory = new Inv();
		invHandler = LazyOptional.of(() -> inventory);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return AdjustableCrateContainer.create(id, inventory, this);
	}

	public AdjustableCrateTileEntity getOtherCrate() {
		if (!AllBlocks.ADJUSTABLE_CRATE.has(getBlockState()))
			return null;
		BlockEntity tileEntity = level.getBlockEntity(worldPosition.relative(getFacing()));
		if (tileEntity instanceof AdjustableCrateTileEntity)
			return (AdjustableCrateTileEntity) tileEntity;
		return null;
	}

	public AdjustableCrateTileEntity getMainCrate() {
		if (isSecondaryCrate())
			return getOtherCrate();
		return this;
	}

	public void onSplit() {
		AdjustableCrateTileEntity other = getOtherCrate();
		if (other == null)
			return;
		if (other == getMainCrate()) {
			other.onSplit();
			return;
		}

		other.allowedAmount = Math.max(1, allowedAmount - 1024);
		for (int slot = 0; slot < other.inventory.getSlots(); slot++)
			other.inventory.setStackInSlot(slot, ItemStack.EMPTY);
		for (int slot = 16; slot < inventory.getSlots(); slot++) {
			other.inventory.setStackInSlot(slot - 16, inventory.getStackInSlot(slot));
			inventory.setStackInSlot(slot, ItemStack.EMPTY);
		}
		allowedAmount = Math.min(1024, allowedAmount);

		invHandler.invalidate();
		invHandler = LazyOptional.of(() -> inventory);
		other.invHandler.invalidate();
		other.invHandler = LazyOptional.of(() -> other.inventory);
	}

	public void onDestroyed() {
		AdjustableCrateTileEntity other = getOtherCrate();
		if (other == null) {
			for (int slot = 0; slot < inventory.getSlots(); slot++)
				drop(slot);
			return;
		}

		AdjustableCrateTileEntity main = getMainCrate();
		if (this == main) {
			for (int slot = 0; slot < inventory.getSlots(); slot++) {
				other.inventory.setStackInSlot(slot, inventory.getStackInSlot(slot));
				inventory.setStackInSlot(slot, ItemStack.EMPTY);
			}
			other.allowedAmount = Math.min(1024, allowedAmount);
		}

		for (int slot = 16; slot < other.inventory.getSlots(); slot++)
			other.drop(slot);

		other.invHandler.invalidate();
		other.invHandler = LazyOptional.of(() -> other.inventory);
	}

	private void drop(int slot) {
		Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inventory.getStackInSlot(slot));
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putBoolean("Main", true);
		compound.putInt("AllowedAmount", allowedAmount);
		compound.put("Inventory", inventory.serializeNBT());
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(CompoundTag compound, boolean clientPacket) {
		allowedAmount = compound.getInt("AllowedAmount");
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		super.fromTag(compound, clientPacket);
	}

	@Override
	public Component getDisplayName() {
		return Lang.translate("gui.adjustable_crate.title");
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		invHandler.invalidate();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			AdjustableCrateTileEntity mainCrate = getMainCrate();
			if (mainCrate != null && mainCrate.invHandler != null && mainCrate.invHandler.isPresent())
				return mainCrate.invHandler.cast();
		}
		return super.getCapability(capability, facing);
	}

}
