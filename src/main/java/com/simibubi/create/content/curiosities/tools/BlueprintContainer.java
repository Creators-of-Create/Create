package com.simibubi.create.content.curiosities.tools;

import java.util.Optional;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.content.curiosities.tools.BlueprintEntity.BlueprintSection;
import com.simibubi.create.foundation.gui.GhostItemContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class BlueprintContainer extends GhostItemContainer<BlueprintSection> {

	public BlueprintContainer(ContainerType<?> type, int id, PlayerInventory inv, PacketBuffer extraData) {
		super(type, id, inv, extraData);
	}

	public BlueprintContainer(ContainerType<?> type, int id, PlayerInventory inv, BlueprintSection section) {
		super(type, id, inv, section);
	}

	public static BlueprintContainer create(int id, PlayerInventory inv, BlueprintSection section) {
		return new BlueprintContainer(AllContainerTypes.CRAFTING_BLUEPRINT.get(), id, inv, section);
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	protected void addSlots() {
		addPlayerSlots(8, 131);

		int x = 29;
		int y = 21;
		int index = 0;
		for (int row = 0; row < 3; ++row)
			for (int col = 0; col < 3; ++col)
				this.addSlot(new BlueprintCraftSlot(ghostInventory, index++, x + col * 18, y + row * 18));

		addSlot(new BlueprintCraftSlot(ghostInventory, index++, 123, 40));
		addSlot(new SlotItemHandler(ghostInventory, index++, 135, 57));
	}

	public void onCraftMatrixChanged() {
		if (contentHolder.getBlueprintWorld().isClientSide)
			return;

		ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
		CraftingInventory craftingInventory = new BlueprintCraftingInventory(this, ghostInventory);
		Optional<ICraftingRecipe> optional = player.getServer()
				.getRecipeManager()
				.getRecipeFor(IRecipeType.CRAFTING, craftingInventory, player.getCommandSenderWorld());

		if (!optional.isPresent()) {
			if (ghostInventory.getStackInSlot(9)
					.isEmpty())
				return;
			if (!contentHolder.inferredIcon)
				return;

			ghostInventory.setStackInSlot(9, ItemStack.EMPTY);
			serverplayerentity.connection.send(new SSetSlotPacket(containerId, 36 + 9, ItemStack.EMPTY));
			contentHolder.inferredIcon = false;
			return;
		}

		ICraftingRecipe icraftingrecipe = optional.get();
		ItemStack itemstack = icraftingrecipe.assemble(craftingInventory);
		ghostInventory.setStackInSlot(9, itemstack);
		contentHolder.inferredIcon = true;
		ItemStack toSend = itemstack.copy();
		toSend.getOrCreateTag()
				.putBoolean("InferredFromRecipe", true);
		serverplayerentity.connection.send(new SSetSlotPacket(containerId, 36 + 9, toSend));
	}

	@Override
	public void setItem(int p_75141_1_, ItemStack p_75141_2_) {
		if (p_75141_1_ == 36 + 9) {
			if (p_75141_2_.hasTag()) {
				contentHolder.inferredIcon = p_75141_2_.getTag()
						.getBoolean("InferredFromRecipe");
				p_75141_2_.getTag()
						.remove("InferredFromRecipe");
			} else
				contentHolder.inferredIcon = false;
		}
		super.setItem(p_75141_1_, p_75141_2_);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return contentHolder.getItems();
	}

	@Override
	protected void readData(BlueprintSection contentHolder) {
	}

	@Override
	protected void saveData(BlueprintSection contentHolder) {
		contentHolder.save(ghostInventory);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected BlueprintSection createOnClient(PacketBuffer extraData) {
		int entityID = extraData.readVarInt();
		int section = extraData.readVarInt();
		Entity entityByID = Minecraft.getInstance().level.getEntity(entityID);
		if (!(entityByID instanceof BlueprintEntity))
			return null;
		BlueprintEntity blueprintEntity = (BlueprintEntity) entityByID;
		BlueprintSection blueprintSection = blueprintEntity.getSection(section);
		return blueprintSection;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return contentHolder != null && contentHolder.canPlayerUse(player);
	}

	static class BlueprintCraftingInventory extends CraftingInventory {

		public BlueprintCraftingInventory(Container container, ItemStackHandler items) {
			super(container, 3, 3);
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					ItemStack stack = items.getStackInSlot(y * 3 + x);
					setItem(y * 3 + x, stack == null ? ItemStack.EMPTY : stack.copy());
				}
			}
		}

	}

	class BlueprintCraftSlot extends SlotItemHandler {

		private int index;

		public BlueprintCraftSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
			super(itemHandler, index, xPosition, yPosition);
			this.index = index;
		}

		@Override
		public void setChanged() {
			super.setChanged();
			if (index == 9 && hasItem() && !contentHolder.getBlueprintWorld().isClientSide) {
				contentHolder.inferredIcon = false;
				ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
				serverplayerentity.connection.send(new SSetSlotPacket(containerId, 36 + 9, getItem()));
			}
			if (index < 9)
				onCraftMatrixChanged();
		}

	}

}
