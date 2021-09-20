package com.simibubi.create.foundation.item;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;

/**
 * Allow's us to register ISTER's in a way which makes registrate not die
 */
public class ISTERItem extends Item implements ISTERCapableItem {

	private IItemRenderProperties itemRenderProperties;

	public ISTERItem(Properties pProperties) {
		super(pProperties);
	}

	public void setRenderProperties(IItemRenderProperties itemRenderProperties) {
		this.itemRenderProperties = itemRenderProperties;
	}

	@Override
	public IItemRenderProperties getRenderProperties() {
		return this.itemRenderProperties;
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		if (Minecraft.getInstance() == null) return;
		if (itemRenderProperties == null) {
			super.initializeClient(consumer);
			return;
		}

		consumer.accept(itemRenderProperties);
	}


}
