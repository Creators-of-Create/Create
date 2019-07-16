package com.simibubi.create.block;

import com.simibubi.create.AllContainers;
import com.simibubi.create.AllItems;
import com.simibubi.create.networking.PacketSchematicTableContainer;
import com.simibubi.create.networking.Packets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.SlotItemHandler;

public class SchematicTableContainer extends Container {

	private SchematicTableTileEntity te;
	private Slot inputSlot;
	private Slot outputSlot;
	private PlayerEntity player;

	public String schematicUploading;
	public boolean isUploading;
	public float progress;
	public boolean sendSchematicUpdate;

	public SchematicTableContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainers.SchematicTable.type, id);
		player = inv.player;
		ClientWorld world = Minecraft.getInstance().world;
		this.te = (SchematicTableTileEntity) world.getTileEntity(extraData.readBlockPos());
		this.te.handleUpdateTag(extraData.readCompoundTag());
		init();
	}

	public SchematicTableContainer(int id, PlayerInventory inv, SchematicTableTileEntity te) {
		super(AllContainers.SchematicTable.type, id);
		this.player = inv.player;
		this.te = te;
		init();
	}

	protected void init() {
		inputSlot = new SlotItemHandler(te.inventory, 0, -9, 40) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return AllItems.EMPTY_BLUEPRINT.typeOf(stack);
			}
		};

		outputSlot = new SlotItemHandler(te.inventory, 1, 75, 40) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}
		};

		addSlot(inputSlot);
		addSlot(outputSlot);
		
		// player Slots
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(player.inventory, col + row * 9 + 9, -8 + col * 18, 102 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.addSlot(new Slot(player.inventory, hotbarSlot, -8 + hotbarSlot * 18, 160));
		}
		
		detectAndSendChanges();
	}

	@Override
	public void detectAndSendChanges() {
		if (te.uploadingSchematic != null) {
			schematicUploading = te.uploadingSchematic;
			progress = te.uploadingProgress;
			isUploading = true;
		} else {
			schematicUploading = null;
			progress = 0;
			isUploading = false;
		}
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
		super.onContainerClosed(playerIn);
	}

	public SchematicTableTileEntity getTileEntity() {
		return te;
	}

}
