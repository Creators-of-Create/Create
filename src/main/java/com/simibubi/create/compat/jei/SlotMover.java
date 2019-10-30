package com.simibubi.create.compat.jei;

import java.util.List;

import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.inventory.container.Container;

/**
 * Allows a {@link AbstractSimiContainerScreen} to specify an area in getExtraArea() that will be avoided by JEI
 *
 * Name is taken from CoFHCore's 1.12 implementation.
 */
public class SlotMover<T extends Container> implements IGuiContainerHandler<AbstractSimiContainerScreen<T>> {

    @Override
    public List<Rectangle2d> getGuiExtraAreas(AbstractSimiContainerScreen<T> containerScreen) {
        return containerScreen.getExtraAreas();
    }
}
