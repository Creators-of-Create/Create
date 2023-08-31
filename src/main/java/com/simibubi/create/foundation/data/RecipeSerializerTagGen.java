package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags.AllRecipeSerializerTags;
import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RecipeSerializerTagGen extends TagsProvider<RecipeSerializer<?>> {
	public RecipeSerializerTagGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
		super(output, Registries.RECIPE_SERIALIZER, lookupProvider, Create.ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Create's Recipe Serializer Tags";
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		this.tag(AllRecipeSerializerTags.AUTOMATION_IGNORE.tag)
				.addOptional(Mods.OCCULTISM.rl("spirit_trade"))
				.addOptional(Mods.OCCULTISM.rl("ritual"));
	}
}
