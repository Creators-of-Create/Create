package com.simibubi.create.compat.curios;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import top.theillusivec4.curios.api.CuriosDataProvider;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CuriosDataGenerator extends CuriosDataProvider {
	public CuriosDataGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper fileHelper) {
		super(Create.ID, output, fileHelper, registries);
	}

	@Override
	public void generate(Provider registries, ExistingFileHelper fileHelper) {
		createEntities("players")
			.addPlayer()
			.addSlots("head");
	}
}
