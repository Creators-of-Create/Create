package com.simibubi.create.compat.jei.category;

import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.material.Fluids;

public class FanWashingCategory extends ProcessingViaFanCategory.MultiOutput<SplashingRecipe> {

	public FanWashingCategory(Info<SplashingRecipe> info) {
		super(info);
	}

	@Override
	protected void renderAttachedBlock(GuiGraphicsExtractor graphics) {
		GuiGameElement.of(Fluids.WATER)
			.scale(SCALE)
			.atLocal(0, 0, 2)
			.lighting(AnimatedKinetics.DEFAULT_LIGHTING)
			.render(graphics, 0, 0, 0);
	}

}
