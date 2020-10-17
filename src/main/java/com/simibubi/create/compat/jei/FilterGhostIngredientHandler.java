package com.simibubi.create.compat.jei;

import com.simibubi.create.content.logistics.item.filter.AbstractFilterContainer;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket;
import com.simibubi.create.foundation.networking.AllPackets;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.apache.logging.log4j.LogManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilterGhostIngredientHandler<T extends AbstractFilterContainer> implements IGhostIngredientHandler<AbstractFilterScreen<T>> {

	@Override
	public <I> List<Target<I>> getTargets(AbstractFilterScreen<T> gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();
		boolean isAttributeFilter = gui instanceof AttributeFilterScreen;

		if (ingredient instanceof ItemStack) {
			for (int i = 36; i < gui.getContainer().inventorySlots.size(); i++) {
				targets.add(new FilterGhostTarget<>(gui, i - 36, isAttributeFilter));

				//Only accept items in 1st slot. 2nd is used for functionality, don't wanna override that one
				if (isAttributeFilter) break;
			}
		}

		return targets;
	}

	@Override
	public void onComplete() {}

	@Override
	public boolean shouldHighlightTargets() {
		//TODO change to false and highlight the slots ourself in some better way
		return true;
	}

	private static class FilterGhostTarget<I, T extends AbstractFilterContainer> implements Target<I> {

		private final Rectangle2d area;
		private final AbstractFilterScreen<T> gui;
		private final int slotIndex;
		private final boolean isAttributeFilter;


		public FilterGhostTarget(AbstractFilterScreen<T> gui, int slotIndex, boolean isAttributeFilter) {
			this.gui = gui;
			this.slotIndex = slotIndex;
			this.isAttributeFilter = isAttributeFilter;
			Slot slot = gui.getContainer().inventorySlots.get(slotIndex + 36);
			this.area = new Rectangle2d(
					gui.getGuiLeft() + slot.xPos,
					gui.getGuiTop() + slot.yPos,
					16,
					16);
		}

		@Override
		public Rectangle2d getArea() {
			return area;
		}

		@Override
		public void accept(I ingredient) {
			ItemStack stack = ((ItemStack) ingredient).copy();
			LogManager.getLogger().info(stack);
			stack.setCount(1);
			gui.getContainer().filterInventory.setStackInSlot(slotIndex, stack);

			if (isAttributeFilter) return;

			//sync new filter contents with server
			CompoundNBT data = new CompoundNBT();
			data.putInt("Slot", slotIndex);
			data.put("Item", stack.serializeNBT());
			AllPackets.channel.sendToServer(new FilterScreenPacket(FilterScreenPacket.Option.UPDATE_FILTER_ITEM, data));
		}
	}
}
