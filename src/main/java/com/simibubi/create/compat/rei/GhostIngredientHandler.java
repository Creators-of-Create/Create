package com.simibubi.create.compat.rei;

import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.content.logistics.item.LinkedControllerScreen;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.container.GhostItemContainer;

import com.simibubi.create.foundation.gui.container.GhostItemSubmitPacket;
import com.simibubi.create.foundation.networking.AllPackets;

import com.simibubi.create.lib.mixin.client.accessor.AbstractContainerScreenAccessor;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GhostIngredientHandler<T extends GhostItemContainer<?>>
		implements DraggableStackVisitor<AbstractSimiContainerScreen<T>> {

	@Override
	public DraggedAcceptorResult acceptDraggedStack(DraggingContext<AbstractSimiContainerScreen<T>> context, DraggableStack stack) {
		Stream<BoundsProvider> bounds = getDraggableAcceptingBounds(context, stack);
		Point cursor = context.getCurrentPosition();
		if (cursor != null) {
			int x = cursor.getX();
			int y = cursor.getY();
			Optional<BoundsProvider> target = bounds.filter(b -> {
				AABB box = b.bounds().bounds();
				double minX = box.minX;
				double minY = box.minY;
				double maxX = box.maxX;
				double maxY = box.maxY;
				return x >= minX && x <= maxX && y >= minY && y <= maxY && b instanceof GhostTarget;
			}).findFirst();
			if (target.isPresent() && target.get() instanceof GhostTarget ghost) {
				Object held = stack.getStack().getValue();
				if (held instanceof ItemStack item) {
					ghost.accept(item);
					return DraggedAcceptorResult.CONSUMED;
				}
			}
		}
		return DraggableStackVisitor.super.acceptDraggedStack(context, stack);
	}

	@Override
	public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<AbstractSimiContainerScreen<T>> context, DraggableStack stack) {
		List<BoundsProvider> targets = new ArrayList<>();
		AbstractSimiContainerScreen<T> gui = context.getScreen();
		boolean isAttributeFilter = gui instanceof AttributeFilterScreen;

		if (stack.getStack().getValue() instanceof ItemStack) {
			for (int i = 36; i < gui.getMenu().slots.size(); i++) {
				targets.add(new GhostTarget<>(gui, i - 36, isAttributeFilter));

				// Only accept items in 1st slot. 2nd is used for functionality, don't wanna override that one
				if (isAttributeFilter)
					break;
			}
		}

		return targets.stream();
	}

	@Override
	public <R extends Screen> boolean isHandingScreen(R screen) {
		return screen instanceof AbstractFilterScreen || screen instanceof BlueprintScreen || screen instanceof LinkedControllerScreen;
	}

	private static class GhostTarget<I, T extends GhostItemContainer<?>> implements BoundsProvider {

		private final Rectangle area;
		private final AbstractSimiContainerScreen<T> gui;
		private final int slotIndex;
		private final boolean isAttributeFilter;

		public GhostTarget(AbstractSimiContainerScreen<T> gui, int slotIndex, boolean isAttributeFilter) {
			this.gui = gui;
			this.slotIndex = slotIndex;
			this.isAttributeFilter = isAttributeFilter;
			Slot slot = gui.getMenu().slots.get(slotIndex + 36);
			AbstractContainerScreenAccessor access = (AbstractContainerScreenAccessor) gui;
			this.area = new Rectangle(access.create$getGuiLeft() + slot.x, access.create$getGuiTop() + slot.y, 16, 16);
		}

		public void accept(I ingredient) {
			ItemStack stack = ((ItemStack) ingredient).copy();
			stack.setCount(1);
			gui.getMenu().ghostInventory.setStackInSlot(slotIndex, stack);

			if (isAttributeFilter)
				return;

			// sync new filter contents with server
			AllPackets.channel.sendToServer(new GhostItemSubmitPacket(stack, slotIndex));
		}

		@Override
		public VoxelShape bounds() {
			return BoundsProvider.fromRectangle(area);
		}
	}
}
