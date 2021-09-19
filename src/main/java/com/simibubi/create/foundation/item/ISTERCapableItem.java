package com.simibubi.create.foundation.item;

import net.minecraftforge.client.IItemRenderProperties;

public interface ISTERCapableItem {

	void setRenderProperties(IItemRenderProperties itemRenderProperties);

	IItemRenderProperties getRenderProperties();
}
