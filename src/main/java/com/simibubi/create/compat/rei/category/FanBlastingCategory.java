package com.simibubi.create.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.display.FanBlastingDisplay;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.material.Fluids;

public class FanBlastingCategory extends ProcessingViaFanCategory<AbstractCookingRecipe, FanBlastingDisplay> {

	public FanBlastingCategory() {
		super(doubleItemIcon(AllItems.PROPELLER, () -> Items.LAVA_BUCKET));
	}

	@Override
	public void renderAttachedBlock(PoseStack matrixStack) {
		matrixStack.pushPose();

		GuiGameElement.of(Fluids.LAVA)
			.scale(24)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(matrixStack);

		matrixStack.popPose();
	}

}
