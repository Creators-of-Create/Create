package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.container.GhostItemContainer;
import com.simibubi.create.foundation.gui.container.GhostItemSubmitPacket;
import com.simibubi.create.foundation.networking.AllPackets;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GhostIngredientHandler<T extends GhostItemContainer<?>>
	implements IGhostIngredientHandler<AbstractSimiContainerScreen<T>> {

	@Override
	public <I> List<Target<I>> getTargets(AbstractSimiContainerScreen<T> gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		boolean isAttributeFilter = gui instanceof AttributeFilterScreen;

		if (ingredient instanceof ItemStack) {
			for (int i = 36; i < gui.getMenu().slots.size(); i++) {
				if (gui.getMenu().slots.get(i)
					.isActive())
					targets.add(new GhostTarget<>(gui, i - 36, isAttributeFilter));

				// Only accept items in 1st slot. 2nd is used for functionality, don't wanna
				// override that one
				if (isAttributeFilter)
					break;
			}
		}

		return targets;
	}

	@Override
	public void onComplete() {}

	@Override
	public boolean shouldHighlightTargets() {
		// TODO change to false and highlight the slots ourself in some better way
		return true;
	}

	private static class GhostTarget<I, T extends GhostItemContainer<?>> implements Target<I> {

		private final Rect2i area;
		private final AbstractSimiContainerScreen<T> gui;
		private final int slotIndex;
		private final boolean isAttributeFilter;

		public GhostTarget(AbstractSimiContainerScreen<T> gui, int slotIndex, boolean isAttributeFilter) {
			this.gui = gui;
			this.slotIndex = slotIndex;
			this.isAttributeFilter = isAttributeFilter;
			Slot slot = gui.getMenu().slots.get(slotIndex + 36);
			this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
		}

		@Override
		public Rect2i getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemStack stack = ((ItemStack) ingredient).copy();
			stack.setCount(1);
			gui.getMenu().ghostInventory.setStackInSlot(slotIndex, stack);

			if (isAttributeFilter)
				return;

			// sync new filter contents with server
			AllPackets.channel.sendToServer(new GhostItemSubmitPacket(stack, slotIndex));
		}
	}
}
