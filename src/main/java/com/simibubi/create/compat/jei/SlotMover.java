package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;

/**
 * Allows a {@link AbstractSimiContainerScreen} to specify an area in getExtraArea() that will be avoided by JEI
 *
 * Name is taken from CoFHCore's 1.12 implementation.
 */
public class SlotMover implements IGuiContainerHandler<AbstractSimiContainerScreen<?>> {

	@Override
	public List<Rect2i> getGuiExtraAreas(AbstractSimiContainerScreen<?> containerScreen) {
		return containerScreen.getExtraAreas();
	}

}
