package com.simibubi.create.content.equipment.blueprint;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.Optional;

public class BlueprintMenu extends GhostItemMenu<BlueprintSection> {

	public BlueprintMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public BlueprintMenu(MenuType<?> type, int id, Inventory inv, BlueprintSection section) {
		super(type, id, inv, section);
	}

	public static BlueprintMenu create(int id, Inventory inv, BlueprintSection section) {
		return new BlueprintMenu(AllMenuTypes.CRAFTING_BLUEPRINT.get(), id, inv, section);
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
		Level level = contentHolder.getBlueprintWorld();
		if (level.isClientSide)
			return;

		ServerPlayer serverplayerentity = (ServerPlayer) player;
		CraftingContainer craftingInventory = new BlueprintCraftingInventory(this, ghostInventory);
		Optional<CraftingRecipe> optional = player.getServer()
				.getRecipeManager()
				.getRecipeFor(RecipeType.CRAFTING, craftingInventory, player.getCommandSenderWorld());

		if (!optional.isPresent()) {
			if (ghostInventory.getStackInSlot(9)
					.isEmpty())
				return;
			if (!contentHolder.inferredIcon)
				return;

			ghostInventory.setStackInSlot(9, ItemStack.EMPTY);
			serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 36 + 9, ItemStack.EMPTY));
			contentHolder.inferredIcon = false;
			return;
		}

		CraftingRecipe icraftingrecipe = optional.get();
		ItemStack itemstack = icraftingrecipe.assemble(craftingInventory, level.registryAccess());
		ghostInventory.setStackInSlot(9, itemstack);
		contentHolder.inferredIcon = true;
		ItemStack toSend = itemstack.copy();
		toSend.getOrCreateTag()
				.putBoolean("InferredFromRecipe", true);
		serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 36 + 9, toSend));
	}

	@Override
	public void setItem(int slotId, int stateId, ItemStack stack) {
		if (slotId == 36 + 9) {
			if (stack.hasTag()) {
				contentHolder.inferredIcon = stack.getTag()
						.getBoolean("InferredFromRecipe");
				stack.getTag()
						.remove("InferredFromRecipe");
			} else
				contentHolder.inferredIcon = false;
		}
		super.setItem(slotId, stateId, stack);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return contentHolder.getItems();
	}

	@Override
	protected void initAndReadInventory(BlueprintSection contentHolder) {
		super.initAndReadInventory(contentHolder);
	}

	@Override
	protected void saveData(BlueprintSection contentHolder) {
		contentHolder.save(ghostInventory);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	protected BlueprintSection createOnClient(FriendlyByteBuf extraData) {
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
	public boolean stillValid(Player player) {
		return contentHolder != null && contentHolder.canPlayerUse(player);
	}

	static class BlueprintCraftingInventory extends TransientCraftingContainer {

		public BlueprintCraftingInventory(AbstractContainerMenu menu, ItemStackHandler items) {
			super(menu, 3, 3);
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
				ServerPlayer serverplayerentity = (ServerPlayer) player;
				serverplayerentity.connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), 36 + 9, getItem()));
			}
			if (index < 9)
				onCraftMatrixChanged();
		}

	}

}
