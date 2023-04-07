package com.simibubi.create.content.logistics.trains;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public interface IHasTrackMaterial {
	TrackMaterial getMaterial();

	default void setMaterial(TrackMaterial material) {}

	static TrackMaterial fromItem(Item item) {
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof IHasTrackMaterial hasTrackMaterial)
			return hasTrackMaterial.getMaterial();
		return TrackMaterial.ANDESITE;
	}
}
