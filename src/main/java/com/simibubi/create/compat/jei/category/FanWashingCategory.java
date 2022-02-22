package com.simibubi.create.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public class FanWashingCategory extends ProcessingViaFanCategory.MultiOutput<SplashingRecipe> {

	public FanWashingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER.get(), Items.WATER_BUCKET));
	}

	@Override
	public Class<? extends SplashingRecipe> getRecipeClass() {
		return SplashingRecipe.class;
	}

	@Override
	protected void renderAttachedBlock(PoseStack matrixStack) {
		GuiGameElement.of(Fluids.WATER)
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(matrixStack);
	}

}
