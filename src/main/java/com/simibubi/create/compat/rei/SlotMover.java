package com.simibubi.create.compat.rei;

import java.util.ArrayList;
import java.util.Collection;

import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;

/**
 * Allows a {@link AbstractSimiContainerScreen} to specify an area in getExtraArea() that will be avoided by JEI
 *
 * Name is taken from CoFHCore's 1.12 implementation.
 */
public class SlotMover implements ExclusionZonesProvider<AbstractSimiContainerScreen<?>> {

	@Override
	public Collection<Rectangle> provide(AbstractSimiContainerScreen<?> containerScreen) {
		Collection<Rectangle> areas = new ArrayList<>();
		containerScreen.getExtraAreas().forEach(rect2i -> areas.add(new Rectangle(rect2i.getX(), rect2i.getY(), rect2i.getWidth(), rect2i.getHeight())));
		return areas;
	}

}
