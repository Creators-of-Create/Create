package com.simibubi.create.lib.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public interface RenderTooltipBorderColorCallback {
	Event<RenderTooltipBorderColorCallback> EVENT =
			EventFactory.createArrayBacked(RenderTooltipBorderColorCallback.class,
					callbacks -> (stack, originalBorderColorStart, originalBorderColorEnd) -> {
						for (RenderTooltipBorderColorCallback callback : callbacks) {
							BorderColorEntry entry = callback.onTooltipBorderColor(stack, originalBorderColorStart, originalBorderColorEnd);
							if (entry != null) {
								return entry;
							}
						}
						return null;
					});

	BorderColorEntry onTooltipBorderColor(ItemStack stack, int originalBorderColorStart, int originalBorderColorEnd);

	class BorderColorEntry {
		private final int borderColorStart;
		private final int borderColorEnd;

		public BorderColorEntry(int start, int end) {
			borderColorStart = start;
			borderColorEnd = end;
		}

		public int getBorderColorStart() {
			return borderColorStart;
		}

		public int getBorderColorEnd() {
			return borderColorEnd;
		}
	}
}
