package com.simibubi.create.block;

import com.simibubi.create.AllContainers;
import com.simibubi.create.AllItems;
import com.simibubi.create.networking.PacketSchematicTableContainer;
import com.simibubi.create.networking.Packets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

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
	private PlayerEntity player;

	public String schematicUploading;
	public boolean isUploading;
	public float progress;
	public boolean sendSchematicUpdate;

	public SchematicTableContainer(int id, PlayerInventory inv) {
		this(id, inv, null);
	}

	public SchematicTableContainer(int id, PlayerInventory inv, SchematicTableTileEntity te) {
		super(AllContainers.SchematicTable.type, id);
		this.player = inv.player;
		this.te = te;

		inputSlot = new Slot(tableInventory, 0, -9, 15) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return AllItems.EMPTY_BLUEPRINT.typeOf(stack);
			}
		};

		outputSlot = new Slot(tableInventory, 1, 75, 15) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}
		};

		addSlot(inputSlot);
		addSlot(outputSlot);

		updateContent();
		
		if (te != null) {
			this.addListener(te);
		}

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
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		sendSchematicInfo();
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
			schematicUploading = te.uploadingSchematic;
			progress = te.uploadingProgress;
			sendSchematicUpdate = true;
		}
	}

	public void sendSchematicInfo() {
		if (player instanceof ServerPlayerEntity) {
			if (sendSchematicUpdate) {
				Packets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
						new PacketSchematicTableContainer(schematicUploading, progress));
				sendSchematicUpdate = false;
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void receiveSchematicInfo(String schematic, float progress) {
		if (schematic.isEmpty()) {
			this.schematicUploading = null;
			this.isUploading = false;
			this.progress = 0;
			return;
		}

		this.isUploading = true;
		this.schematicUploading = schematic;
		this.progress = .5f;
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
