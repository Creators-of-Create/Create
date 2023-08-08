package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RecipeTypeTagGen extends TagsProvider<RecipeType<?>> {
	public RecipeTypeTagGen(String namespace, PackOutput pOutput,
			CompletableFuture<HolderLookup.Provider> pLookupProvider, ExistingFileHelper existingFileHelper) {
		super(pOutput, Registries.RECIPE_TYPE, pLookupProvider, namespace, existingFileHelper);
	}
	
	public RecipeTypeTagGen(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, 
			ExistingFileHelper existingFileHelper) {
		this(Create.ID, pOutput, pLookupProvider, existingFileHelper);
	}

	@Override
	protected void addTags(@NotNull Provider pProvider) {
		tag(AllTags.AllRecipeTypeTags.AUTOMATION_IGNORE.tag)
			.addOptional(new ResourceLocation("occultism", "spirit_trade"))
			.addOptional(new ResourceLocation("occultism", "ritual"));
	}
}
