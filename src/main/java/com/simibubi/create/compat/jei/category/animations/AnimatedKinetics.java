package com.simibubi.create.compat.jei.category.animations;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.widgets.AnimatedKineticsWidget;
import com.simibubi.create.foundation.gui.CustomLightingSettings;
import com.simibubi.create.foundation.gui.ILightingSettings;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class AnimatedKinetics extends AnimatedKineticsWidget {

	public int offset = 0;

	public static final ILightingSettings DEFAULT_LIGHTING = CustomLightingSettings.builder()
			.firstLightRotation(12.5f, 45.0f)
			.secondLightRotation(-20.0f, 50.0f)
			.build();

	/**
	 * <b>Only use this method outside of subclasses.</b>
	 * Use {@link #blockElement(BlockState)} if calling from inside a subclass.
	 */
	public static GuiGameElement.GuiRenderBuilder defaultBlockElement(BlockState state) {
		return GuiGameElement.of(state)
				.lighting(DEFAULT_LIGHTING);
	}

	/**
	 * <b>Only use this method outside of subclasses.</b>
	 * Use {@link #blockElement(PartialModel)} if calling from inside a subclass.
	 */
	public static GuiGameElement.GuiRenderBuilder defaultBlockElement(PartialModel partial) {
		return GuiGameElement.of(partial)
				.lighting(DEFAULT_LIGHTING);
	}

	public static float getCurrentAngle() {
		return (AnimationTickHolder.getRenderTime() * 4f) % 360;
	}

	protected BlockState shaft(Axis axis) {
		return AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
	}

	protected PartialModel cogwheel() {
		return AllBlockPartials.SHAFTLESS_COGWHEEL;
	}

	protected GuiGameElement.GuiRenderBuilder blockElement(BlockState state) {
		return defaultBlockElement(state);
	}

	protected GuiGameElement.GuiRenderBuilder blockElement(PartialModel partial) {
		return defaultBlockElement(partial);
	}


	//	@Override
//	public int getWidth() {
//		return 50;
//	}
//
//	@Override
//	public int getHeight() {
//		return 50;
//	}

}
