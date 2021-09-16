package com.simibubi.create.content.curiosities.toolbox;

import static com.simibubi.create.content.curiosities.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.foundation.gui.ContainerBase;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.SlotItemHandler;

public class ToolboxContainer extends ContainerBase<ToolboxTileEntity> {

	public static ToolboxContainer create(int id, PlayerInventory inv, ToolboxTileEntity te) {
		return new ToolboxContainer(AllContainerTypes.TOOLBOX.get(), id, inv, te);
	}

	public ToolboxContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id, inv, extraData);
	}

	public ToolboxContainer(ContainerType<?> type, int id, PlayerInventory inv, ToolboxTileEntity te) {
		super(type, id, inv, te);
		te.startOpen(player);
	}

	@Override
	protected ToolboxTileEntity createOnClient(PacketBuffer extraData) {
		BlockPos readBlockPos = extraData.readBlockPos();
		CompoundNBT readNbt = extraData.readNbt();

		ClientWorld world = Minecraft.getInstance().level;
		TileEntity tileEntity = world.getBlockEntity(readBlockPos);
		if (tileEntity instanceof ToolboxTileEntity) {
			ToolboxTileEntity toolbox = (ToolboxTileEntity) tileEntity;
			toolbox.handleUpdateTag(toolbox.getBlockState(), readNbt);
			return toolbox;
		}

		return null;
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		Slot clickedSlot = getSlot(index);
		if (!clickedSlot.hasItem())
			return ItemStack.EMPTY;

		ItemStack stack = clickedSlot.getItem();
		int size = contentHolder.inventory.getSlots();
		if (index < size) {
			moveItemStackTo(stack, size, slots.size(), false);
			contentHolder.inventory.onContentsChanged(index);
		} else
			moveItemStackTo(stack, 0, size - 1, false);

		return ItemStack.EMPTY;
	}

	@Override
	protected void initAndReadInventory(ToolboxTileEntity contentHolder) {

	}

	@Override
	public ItemStack clicked(int index, int flags, ClickType type, PlayerEntity player) {
		int size = contentHolder.inventory.getSlots();

		if (index >= 0 && index < size) {

			ItemStack itemInClickedSlot = getSlot(index).getItem();
			PlayerInventory playerInv = player.inventory;
			ItemStack carried = playerInv.getCarried();

			if (type == ClickType.PICKUP && !carried.isEmpty() && !itemInClickedSlot.isEmpty()
				&& ItemHandlerHelper.canItemStacksStack(itemInClickedSlot, carried)) {
				int subIndex = index % STACKS_PER_COMPARTMENT;
				if (subIndex != STACKS_PER_COMPARTMENT - 1)
					return clicked(index - subIndex + STACKS_PER_COMPARTMENT - 1, flags, type, player);
			}

			if (type == ClickType.PICKUP && !carried.isEmpty() && itemInClickedSlot.isEmpty())
				contentHolder.inventory.filters.set(index / STACKS_PER_COMPARTMENT, carried);
				
			if (type == ClickType.PICKUP && carried.isEmpty() && itemInClickedSlot.isEmpty())
				if (!player.level.isClientSide) {
					contentHolder.inventory.filters.set(index / STACKS_PER_COMPARTMENT, ItemStack.EMPTY);
					contentHolder.sendData();
				}

		}
		return super.clicked(index, flags, type, player);
	}

	@Override
	public boolean canDragTo(Slot slot) {
		return slot.index > contentHolder.inventory.getSlots() && super.canDragTo(slot);
	}

	public ItemStack getFilter(int compartment) {
		return contentHolder.inventory.filters.get(compartment);
	}

	public int totalCountInCompartment(int compartment) {
		int count = 0;
		int baseSlot = compartment * STACKS_PER_COMPARTMENT;
		for (int i = 0; i < STACKS_PER_COMPARTMENT; i++)
			count += getSlot(baseSlot + i).getItem()
				.getCount();
		return count;
	}

	public boolean renderPass;

	@Override
	protected void addSlots() {
		ToolboxInventory inventory = contentHolder.inventory;

		int x = 59;
		int y = 37;

		int[] xOffsets = { x, x + 33, x + 66, x + 66 + 6, x + 66, x + 33, x, x - 6 };
		int[] yOffsets = { y, y - 6, y, y + 33, y + 66, y + 66 + 6, y + 66, y + 33 };

		for (int compartment = 0; compartment < 8; compartment++) {
			int baseIndex = compartment * STACKS_PER_COMPARTMENT;

			// Representative Slots
			addSlot(new ToolboxSlot(this, inventory, baseIndex, xOffsets[compartment], yOffsets[compartment]));

			// Hidden Slots
			for (int i = 1; i < STACKS_PER_COMPARTMENT; i++)
				addSlot(new SlotItemHandler(inventory, baseIndex + i, -100, -100));
		}

		addPlayerSlots(-12, 166);
	}

	@Override
	protected void saveData(ToolboxTileEntity contentHolder) {

	}

	@Override
	public void removed(PlayerEntity playerIn) {
		super.removed(playerIn);
		if (!playerIn.level.isClientSide)
			contentHolder.stopOpen(playerIn);
	}

}
