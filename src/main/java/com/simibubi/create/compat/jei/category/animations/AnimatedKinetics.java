package com.simibubi.create.compat.jei.category.animations;

import com.simibubi.create.foundation.utility.AnimationTickHolder;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;

public abstract class AnimatedKinetics implements IDrawable {

	public static float getCurrentAngle() {
		return ((AnimationTickHolder.ticks + Minecraft.getInstance().getRenderPartialTicks()) * 4f) % 360;
	}

}
