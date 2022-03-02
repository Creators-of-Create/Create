package com.simibubi.create.compat.jei.category;

import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.content.contraptions.processing.fan.custom.CustomFanProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.fan.custom.TypeCustom;
import com.simibubi.create.foundation.gui.element.GuiGameElement;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@ParametersAreNonnullByDefault
public class CustomFanCategory extends ProcessingViaFanCategory.MultiOutput<CustomFanProcessingRecipe> {

	public CustomFanCategory() {
		super(185, doubleItemIcon(AllItems.PROPELLER.get(), Items.BUCKET));
	}

	@Override
	public @NotNull Class<? extends CustomFanProcessingRecipe> getRecipeClass() {
		return CustomFanProcessingRecipe.class;
	}

	@Override
	public void renderAttachedBlock(PoseStack matrixStack, CustomFanProcessingRecipe recipe) {
		AbstractFanProcessingType type = AbstractFanProcessingType.valueOf(recipe.type);
		if (type instanceof TypeCustom custom) {
			GuiGameElement.of(Optional.ofNullable(custom.getConfig().block().getBlockForDisplay()).orElse(Blocks.AIR.defaultBlockState()))
					.scale(24)
					.atLocal(0, 0, 2)
					.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
					.render(matrixStack);
		}
	}

}
