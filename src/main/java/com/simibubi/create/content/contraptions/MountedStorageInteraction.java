package com.simibubi.create.content.contraptions;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MountedStorageInteraction {

	public static final List<MenuType<?>> menus = ImmutableList.of(MenuType.GENERIC_9x1, MenuType.GENERIC_9x2,
		MenuType.GENERIC_9x3, MenuType.GENERIC_9x4, MenuType.GENERIC_9x5, MenuType.GENERIC_9x6);

	public static MenuProvider createMenuProvider(Component displayName, IItemHandlerModifiable handler,
		int slotCount, Supplier<Boolean> stillValid) {
		int rows = Mth.clamp(slotCount / 9, 1, 6);
		MenuType<?> menuType = menus.get(rows - 1);
		Component menuName = CreateLang.translateDirect("contraptions.moving_container", displayName);

		return new MenuProvider() {

			@Override
			public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
				return new ChestMenu(menuType, pContainerId, pPlayerInventory, new StorageInteractionContainer(handler, stillValid),
					rows);
			}

			@Override
			public Component getDisplayName() {
				return menuName;
			}

		};
	}

	public static class StorageInteractionContainer extends RecipeWrapper {

		private Supplier<Boolean> stillValid;

		public StorageInteractionContainer(IItemHandlerModifiable inv, Supplier<Boolean> stillValid) {
			super(inv);
			this.stillValid = stillValid;
		}

		@Override
		public boolean stillValid(Player player) {
			return stillValid.get();
		}

		@Override
		public int getMaxStackSize() {
			return 64;
		}

	}

}
