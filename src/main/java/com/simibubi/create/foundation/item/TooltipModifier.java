package com.simibubi.create.foundation.item;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

public interface TooltipModifier {
	AttachedRegistry<Item, TooltipModifier> REGISTRY = new AttachedRegistry<>(ForgeRegistries.ITEMS);

	TooltipModifier EMPTY = new TooltipModifier() {
		@Override
		public void modify(ItemTooltipEvent context) {
		}

		@Override
		public TooltipModifier andThen(TooltipModifier after) {
			return after;
		}
	};

	void modify(ItemTooltipEvent context);

	default TooltipModifier andThen(TooltipModifier after) {
		if (after == EMPTY) {
			return this;
		}
		return tooltip -> {
			modify(tooltip);
			after.modify(tooltip);
		};
	}

	static TooltipModifier mapNull(@Nullable TooltipModifier modifier) {
		if (modifier == null) {
			return EMPTY;
		}
		return modifier;
	}
}
