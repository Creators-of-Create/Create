package com.simibubi.create.foundation.gui.element;

import com.jozufozu.flywheel.core.PartialModel;

import net.createmod.catnip.gui.element.GuiGameElement;


public class PartialModelGuiElement extends GuiGameElement {

	public static GuiRenderBuilder of(PartialModel partial) {
		return new GuiBlockPartialRenderBuilder(partial);
	}

	public static class GuiBlockPartialRenderBuilder extends GuiBlockModelRenderBuilder {

		public GuiBlockPartialRenderBuilder(PartialModel partial) {
			super(partial.get(), null);
		}

	}

}
