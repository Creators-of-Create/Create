package com.simibubi.create.foundation.data;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags;
import com.simibubi.create.Create;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RecipeSerializerTagGen extends TagsProvider<RecipeSerializer<?>> {
	public RecipeSerializerTagGen(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
		super(pGenerator, Registry.RECIPE_SERIALIZER, Create.ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Create Recipe Serializer Tags";
	}

	@Override
	protected void addTags() {
		this.tag(AllTags.AllRecipeSerializerTags.AUTOMATION_IGNORE.tag)
				.addOptional(new ResourceLocation("occultism", "spirit_trade"))
				.addOptional(new ResourceLocation("occultism", "ritual"));
	}
}
