package com.simibubi.create.modules.contraptions.components.crafter;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.components.crafter.ConnectedInputHandler.ConnectedInput;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MechanicalCrafterTileEntity extends KineticTileEntity {

	enum Phase {
		IDLE, ACCEPTING, ASSEMBLING, EXPORTING
	}

	protected ItemStackHandler inventory = new ItemStackHandler(1) {

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		};

		protected void onContentsChanged(int slot) {
			markDirty();
			sendData();
		};

	};

	protected ConnectedInput input = new ConnectedInput();
	protected LazyOptional<IItemHandler> invSupplier = LazyOptional.of(() -> input.getItemHandler(world, pos));
	protected boolean reRender;

	public MechanicalCrafterTileEntity() {
		super(AllTileEntities.MECHANICAL_CRAFTER.type);
	}

	@Override
	public boolean hasFastRenderer() {
		return false;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Inventory", inventory.serializeNBT());
		CompoundNBT inputNBT = new CompoundNBT();
		input.write(inputNBT);
		compound.put("ConnectedInput", inputNBT);
		return super.write(compound);
	}

	@Override
	public CompoundNBT writeToClient(CompoundNBT tag) {
		if (reRender) {
			tag.putBoolean("Redraw", true);
			reRender = false;
		}
		return super.writeToClient(tag);
	}

	@Override
	public void readClientUpdate(CompoundNBT tag) {
		if (tag.contains("Redraw"))
			world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 16);
		super.readClientUpdate(tag);
	}

	@Override
	public void read(CompoundNBT compound) {
		inventory.deserializeNBT(compound.getCompound("Inventory"));
		input.read(compound.getCompound("ConnectedInput"));
		super.read(compound);
	}

	@Override
	public void remove() {
		invSupplier.invalidate();
		super.remove();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (getBlockState().get(MechanicalCrafterBlock.HORIZONTAL_FACING) == side)
				return LazyOptional.empty();
			return invSupplier.cast();
		}
		return super.getCapability(cap, side);
	}

	public void connectivityChanged() {
		reRender = true;
		sendData();
		invSupplier.invalidate();
		invSupplier = LazyOptional.of(() -> input.getItemHandler(world, pos));
	}

}
