package com.simibubi.create.foundation.data;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllTags.AllRecipeSerializerTags;
import com.simibubi.create.Create;
import com.simibubi.create.compat.Mods;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.data.ExistingFileHelper;

public class RecipeSerializerTagGen extends TagsProvider<RecipeSerializer<?>> {
	public RecipeSerializerTagGen(DataGenerator generator, @Nullable ExistingFileHelper existingFileHelper) {
		super(generator, Registry.RECIPE_SERIALIZER, Create.ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Create's Recipe Serializer Tags";
	}

	@Override
	protected void addTags() {
		this.tag(AllRecipeSerializerTags.AUTOMATION_IGNORE.tag)
				.addOptional(Mods.OCCULTISM.rl("spirit_trade"))
				.addOptional(Mods.OCCULTISM.rl("ritual"));
	}
}
