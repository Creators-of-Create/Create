package com.simibubi.create.foundation.item.render;

import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;

public class SimpleCustomRenderer implements IItemRenderProperties {

	protected CustomRenderedItemModelRenderer renderer;

	protected SimpleCustomRenderer(CustomRenderedItemModelRenderer renderer) {
		this.renderer = renderer;
	}

	public static SimpleCustomRenderer create(Item item, CustomRenderedItemModelRenderer renderer) {
		CustomRenderedItems.register(item);
		return new SimpleCustomRenderer(renderer);
	}

	@Override
	public CustomRenderedItemModelRenderer getItemStackRenderer() {
		return renderer;
	}

}
