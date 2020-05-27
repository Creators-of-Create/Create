package com.simibubi.create.compat.jei.category;

import com.simibubi.create.AllItemsNew;
import com.simibubi.create.foundation.gui.GuiGameElement;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.SmokingRecipe;

public class SmokingViaFanCategory extends ProcessingViaFanCategory<SmokingRecipe> {

	public SmokingViaFanCategory() {
		super("smoking_via_fan", doubleItemIcon(AllItemsNew.PROPELLER.get(), Items.BLAZE_POWDER));
	}

	@Override
	public Class<? extends SmokingRecipe> getRecipeClass() {
		return SmokingRecipe.class;
	}

	@Override
	public void renderAttachedBlock() {

		GuiGameElement.of(Blocks.FIRE.getDefaultState())
				.scale(24)
				.atLocal(0, 0, 2)
				.render();

	}
}