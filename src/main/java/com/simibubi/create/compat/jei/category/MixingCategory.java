package com.simibubi.create.compat.jei.category;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.compat.jei.category.animations.AnimatedMixer;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.HeatCondition;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;

public class MixingCategory extends BasinCategory {

	private final AnimatedMixer mixer = new AnimatedMixer();
	private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();
	private MixingType type;

	enum MixingType {
		AUTO_SHAPELESS, MIXING, AUTO_BREWING;
	}

	public static MixingCategory autoShapeless() {
		return new MixingCategory(MixingType.AUTO_SHAPELESS, Items.CRAFTING_TABLE, 85);
	}

	public static MixingCategory standard() {
		return new MixingCategory(MixingType.MIXING, AllBlocks.BASIN.get(), 103);
	}

	public static MixingCategory autoBrewing() {
		return new MixingCategory(MixingType.AUTO_BREWING, Blocks.BREWING_STAND, 103);
	}

	protected MixingCategory(MixingType type, IItemProvider secondaryItem, int height) {
		super(type != MixingType.AUTO_SHAPELESS, doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), secondaryItem),
			emptyBackground(177, height));
		this.type = type;
	}

	@Override
	public void draw(BasinRecipe recipe, double mouseX, double mouseY) {
		super.draw(recipe, mouseX, mouseY);
		HeatCondition requiredHeat = recipe.getRequiredHeat();
		if (requiredHeat != HeatCondition.NONE)
			heater.withHeat(requiredHeat.visualizeAsBlazeBurner())
				.draw(getBackground().getWidth() / 2 + 3, 55);
		mixer.draw(getBackground().getWidth() / 2 + 3, 34);
	}

}
