package com.simibubi.create.block;

import com.simibubi.create.AllContainers;
import com.simibubi.create.AllItems;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SchematicTableContainer extends Container {

	private final IInventory tableInventory = new Inventory(2) {
		public void markDirty() {
			super.markDirty();
			onCraftMatrixChanged(this);
		}
	};

	private SchematicTableTileEntity te;
	private Slot inputSlot;
	private Slot outputSlot;

	public SchematicTableContainer(int id, PlayerInventory inv) {
		this(id, inv, null);
	}
	
	public SchematicTableContainer(int id, PlayerInventory inv, SchematicTableTileEntity te) {
		super(AllContainers.SchematicTable.type, id);
		this.te = te;

		inputSlot = new Slot(tableInventory, 0, 31, 15) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return AllItems.EMPTY_BLUEPRINT.typeOf(stack);
			}
		};

		outputSlot = new Slot(tableInventory, 1, 115, 15) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}
		};

		addSlot(inputSlot);
		addSlot(outputSlot);

		updateContent();
		
		// player Slots
		tableInventory.openInventory(inv.player);
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(inv, j1 + l * 9 + 9, -8 + j1 * 18, 77 + l * 18));
			}
		}

		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(inv, i1, -8 + i1 * 18, 135));
		}
	}

	public boolean canWrite() {
		return inputSlot.getHasStack() && !outputSlot.getHasStack();
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.getHasStack())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getStack();
		if (clickedSlot == inputSlot || clickedSlot == outputSlot) {
			int indexToPut = playerIn.inventory.getFirstEmptyStack();

			if (indexToPut == -1)
				return ItemStack.EMPTY;

			playerIn.inventory.setInventorySlotContents(indexToPut, stack);
			clickedSlot.putStack(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}

		if (AllItems.EMPTY_BLUEPRINT.typeOf(stack) && !inputSlot.getHasStack()) {
			clickedSlot.putStack(ItemStack.EMPTY);
			inputSlot.putStack(stack);
		}

		return ItemStack.EMPTY;
	}
	
	public void updateContent() {
		if (te != null) {
			inputSlot.putStack(te.inputStack);
			outputSlot.putStack(te.outputStack);
		}
	}
	
	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		if (te != null) {
			te.inputStack = inputSlot.getStack();
			te.outputStack = outputSlot.getStack();
			te.markDirty();
		}
		
		super.onContainerClosed(playerIn);
	}
	
	public SchematicTableTileEntity getTileEntity() {
		return te;
	}

}
