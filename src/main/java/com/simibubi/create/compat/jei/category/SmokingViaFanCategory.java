package com.simibubi.create.compat.jei.category;

import com.simibubi.create.AllItems;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.DoubleItemIcon;
import com.simibubi.create.foundation.gui.ScreenElementRenderer;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.util.ResourceLocation;

public class SmokingViaFanCategory extends ProcessingViaFanCategory<SmokingRecipe> {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "smoking_via_fan");
	private IDrawable icon;

	public SmokingViaFanCategory() {
		icon = new DoubleItemIcon(() -> new ItemStack(AllItems.PROPELLER.get()),
				() -> new ItemStack(Items.BLAZE_POWDER));
	}
	
	@Override
	public IDrawable getIcon() {
		return icon;
	}
	
	@Override
	public ResourceLocation getUid() {
		return ID;
	}

	@Override
	public Class<? extends SmokingRecipe> getRecipeClass() {
		return SmokingRecipe.class;
	}

	@Override
	public String getTitle() {
		return Lang.translate("recipe.smokingViaFan");
	}

	@Override
	public void renderAttachedBlock() {
		ScreenElementRenderer.renderBlock(() -> Blocks.FIRE.getDefaultState());
	}
}