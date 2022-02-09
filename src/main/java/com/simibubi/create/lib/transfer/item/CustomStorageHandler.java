package com.simibubi.create.lib.transfer.item;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

@SuppressWarnings("UnstableApiUsage")
public interface CustomStorageHandler {
	Storage<ItemVariant> getStorage();
}
