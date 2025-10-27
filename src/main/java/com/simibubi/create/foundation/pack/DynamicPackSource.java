package com.simibubi.create.foundation.pack;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

// TODO - Move into catnip
public record DynamicPackSource(String packId, PackType packType, Pack.Position packPosition,
								PackResources packResources) implements RepositorySource {
	@Override
	public void loadPacks(@NotNull Consumer<Pack> onLoad) {
		PackLocationInfo locationInfo = new PackLocationInfo(packId, Component.literal(packId), PackSource.BUILT_IN, Optional.empty());
		PackSelectionConfig selectionConfig = new PackSelectionConfig(true, packPosition, true);
		ResourcesSupplier resourcesSupplier = new ResourcesSupplier() {
			@Override
			public @NotNull PackResources openPrimary(@NotNull PackLocationInfo packLocationInfo) {
				return packResources;
			}

			@Override
			public @NotNull PackResources openFull(@NotNull PackLocationInfo packLocationInfo, @NotNull Metadata metadata) {
				return packResources;
			}
		};
		onLoad.accept(Pack.readMetaAndCreate(locationInfo, resourcesSupplier, packType, selectionConfig));
	}
}
