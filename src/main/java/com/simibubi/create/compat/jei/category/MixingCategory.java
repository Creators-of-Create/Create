package com.simibubi.create.compat.jei.category;

import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MixingCategory extends BasinCategory {

	private final AnimatedMixer mixer = new AnimatedMixer();
	private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
	MixingType type;

	enum MixingType {
		MIXING, AUTO_SHAPELESS, AUTO_BREWING
	}

	public static MixingCategory standard(Info<BasinRecipe> info) {
		return new MixingCategory(info, MixingType.MIXING);
	}

	public static MixingCategory autoShapeless(Info<BasinRecipe> info) {
		return new MixingCategory(info, MixingType.AUTO_SHAPELESS);
	}

	public static MixingCategory autoBrewing(Info<BasinRecipe> info) {
		return new MixingCategory(info, MixingType.AUTO_BREWING);
	}

	protected MixingCategory(Info<BasinRecipe> info, MixingType type) {
		super(info, type != MixingType.AUTO_SHAPELESS);
		this.type = type;
	}

	@Override
	public void draw(BasinRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		super.draw(recipe, iRecipeSlotsView, graphics, mouseX, mouseY);

		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE)
			heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
				.draw(graphics, getBackground().getWidth() / 2 + 3, 55);
		mixer.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
	}

}
