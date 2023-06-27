package com.simibubi.create.foundation.item.render;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class SimpleCustomRenderer implements IClientItemExtensions {

	protected CustomRenderedItemModelRenderer renderer;

	protected SimpleCustomRenderer(CustomRenderedItemModelRenderer renderer) {
		this.renderer = renderer;
	}

	public static SimpleCustomRenderer create(Item item, CustomRenderedItemModelRenderer renderer) {
		CustomRenderedItems.register(item);
		return new SimpleCustomRenderer(renderer);
	}

	@Override
	public CustomRenderedItemModelRenderer getCustomRenderer() {
		return renderer;
	}

}
