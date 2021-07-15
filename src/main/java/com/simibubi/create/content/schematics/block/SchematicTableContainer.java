package com.simibubi.create.content.schematics.block;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.AllItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
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

	public SchematicTableContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id);
		player = inv.player;
		ClientWorld world = Minecraft.getInstance().level;
		TileEntity tileEntity = world.getBlockEntity(extraData.readBlockPos());
		if (tileEntity instanceof SchematicTableTileEntity) {
			this.te = (SchematicTableTileEntity) tileEntity;
			this.te.handleUpdateTag(te.getBlockState(), extraData.readNbt());
			init();
		}
	}

	public SchematicTableContainer(ContainerType<?> type, int id, PlayerInventory inv, SchematicTableTileEntity te) {
		super(type, id);
		this.player = inv.player;
		this.te = te;
		init();
	}

	public static SchematicTableContainer create(int id, PlayerInventory inv, SchematicTableTileEntity te) {
		return new SchematicTableContainer(AllContainerTypes.SCHEMATIC_TABLE.get(), id, inv, te);
	}

	protected void init() {
		inputSlot = new SlotItemHandler(te.inventory, 0, 21, 57) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return AllItems.EMPTY_SCHEMATIC.isIn(stack) || AllItems.SCHEMATIC_AND_QUILL.isIn(stack)
						|| AllItems.SCHEMATIC.isIn(stack);
			}
		};

		outputSlot = new SlotItemHandler(te.inventory, 1, 166, 57) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		};

		addSlot(inputSlot);
		addSlot(outputSlot);

		// player Slots
		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(player.inventory, col + row * 9 + 9, 38 + col * 18, 105 + row * 18));
			}
		}

		for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			this.addSlot(new Slot(player.inventory, hotbarSlot, 38 + hotbarSlot * 18, 163));
		}

		broadcastChanges();
	}

	public boolean canWrite() {
		return inputSlot.hasItem() && !outputSlot.hasItem();
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return te != null && te.canPlayerUse(player);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		if (index < 2)
			moveItemStackTo(stack, 2, slots.size(), false);
		else
			moveItemStackTo(stack, 0, 1, false);

		return ItemStack.EMPTY;
	}

	public SchematicTableTileEntity getTileEntity() {
		return te;
	}

}
