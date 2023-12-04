package com.simibubi.create.infrastructure.data;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags.AllRecipeSerializerTags;
import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.data.ExistingFileHelper;

public class CreateRecipeSerializerTagsProvider extends TagsProvider<RecipeSerializer<?>> {
	public CreateRecipeSerializerTagsProvider(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
		super(generator, Registry.RECIPE_SERIALIZER, Create.ID, existingFileHelper);
	}

	@Override
	protected void addTags() {
		tag(AllRecipeSerializerTags.AUTOMATION_IGNORE.tag)
			.addOptional(Mods.OCCULTISM.rl("spirit_trade"))
			.addOptional(Mods.OCCULTISM.rl("ritual"));

		// VALIDATE

		for (AllRecipeSerializerTags tag : AllRecipeSerializerTags.values()) {
			if (tag.alwaysDatagen) {
				getOrCreateRawBuilder(tag.tag);
			}
		}
	}

	@Override
	public String getName() {
		return "Create's Recipe Serializer Tags";
	}
}
