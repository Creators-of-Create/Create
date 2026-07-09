package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags.AllMountedItemStorageTypeTags;
import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;

public class CreateMountedItemStorageTypeTagsProvider extends TagsProvider<MountedItemStorageType<?>> {
	public CreateMountedItemStorageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, lookupProvider, Create.ID);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(AllMountedItemStorageTypeTags.INTERNAL.tag).add(
			AllMountedStorageTypes.DISPENSER.getKey()
		);
		tag(AllMountedItemStorageTypeTags.FUEL_BLACKLIST.tag).add(
			AllMountedStorageTypes.VAULT.getKey()
		);
	}

	@Override
	public String getName() {
		return "Create's Mounted Item Storage Type Tags";
	}
}
