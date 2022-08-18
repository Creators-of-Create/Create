package com.simibubi.create.compat.jei.category;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.material.Fluids;

public class FanBlastingCategory extends ProcessingViaFanCategory<AbstractCookingRecipe> {

	public FanBlastingCategory(Info<AbstractCookingRecipe> info) {
		super(info);
	}

	@Override
	protected AllGuiTextures getBlockShadow() {
		return AllGuiTextures.JEI_LIGHT;
	}

	@Override
	protected void renderAttachedBlock(@NotNull PoseStack matrixStack) {
		GuiGameElement.of(Fluids.LAVA)
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(matrixStack);
	}

}
