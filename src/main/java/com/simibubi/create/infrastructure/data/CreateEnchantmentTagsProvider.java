package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class CreateEnchantmentTagsProvider extends EnchantmentTagsProvider {
	public CreateEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, Create.ID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider prov) {
		tag(EnchantmentTags.NON_TREASURE)
			.add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
		tag(EnchantmentTags.IN_ENCHANTING_TABLE)
			.add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
	}
}
