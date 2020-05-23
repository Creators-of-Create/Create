package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.AllItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.SlotItemHandler;

public class SchematicTableContainer extends Container {

	private SchematicTableTileEntity te;
	private Slot inputSlot;
	private Slot outputSlot;
	private PlayerEntity player;

	public SchematicTableContainer(int id, PlayerInventory inv, PacketBuffer extraData) {
		super(AllContainerTypes.SCHEMATIC_TABLE.type, id);
		player = inv.player;
		ClientWorld world = Minecraft.getInstance().world;
		TileEntity tileEntity = world.getTileEntity(extraData.readBlockPos());
		if (tileEntity instanceof SchematicTableTileEntity) {
			this.te = (SchematicTableTileEntity) tileEntity;
			this.te.handleUpdateTag(extraData.readCompoundTag());
			init();
		}
	}

	public SchematicTableContainer(int id, PlayerInventory inv, SchematicTableTileEntity te) {
		super(AllContainerTypes.SCHEMATIC_TABLE.type, id);
		this.player = inv.player;
		this.te = te;
		init();
	}

	protected void init() {
		inputSlot = new SlotItemHandler(te.inventory, 0, -9, 40) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return AllItems.EMPTY_BLUEPRINT.typeOf(stack) || AllItems.BLUEPRINT_AND_QUILL.typeOf(stack)
						|| AllItems.BLUEPRINT.typeOf(stack);
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
				this.addSlot(new Slot(player.inventory, col + row * 9 + 9, 12 + col * 18, 102 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.addSlot(new Slot(player.inventory, hotbarSlot, 12 + hotbarSlot * 18, 160));
		}

		detectAndSendChanges();
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
		if (index < 2)
			mergeItemStack(stack, 2, inventorySlots.size(), false);
		else
			mergeItemStack(stack, 0, 1, false);

		return ItemStack.EMPTY;
	}

	public SchematicTableTileEntity getTileEntity() {
		return te;
	}

}
