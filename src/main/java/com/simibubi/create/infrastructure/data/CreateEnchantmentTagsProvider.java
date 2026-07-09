package com.simibubi.create.infrastructure.data;

import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;

public class CreateEnchantmentTagsProvider extends EnchantmentTagsProvider {
	public CreateEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(output, lookupProvider, Create.ID);
	}

	@Override
	protected void addTags(Provider prov) {
		tag(EnchantmentTags.NON_TREASURE)
			.add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
		tag(EnchantmentTags.IN_ENCHANTING_TABLE)
			.add(AllEnchantments.CAPACITY, AllEnchantments.POTATO_RECOVERY);
	}
}
