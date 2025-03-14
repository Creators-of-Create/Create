package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Just in case players don't know about that vanilla feature
 */
public class LogStrippingFakeRecipes {

	public static List<RecipeHolder<ManualApplicationRecipe>> createRecipes() {
		List<RecipeHolder<ManualApplicationRecipe>> recipes = new ArrayList<>();
		if (!AllConfigs.server().recipes.displayLogStrippingRecipes.get())
			return recipes;

		ItemStack axe = new ItemStack(Items.IRON_AXE);
		axe.set(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
		axe.set(DataComponents.CUSTOM_NAME, CreateLang.translateDirect("recipe.item_application.any_axe")
			.withStyle(style -> style.withItalic(false)));
		BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.LOGS)
				.forEach(stack -> process(stack.value(), recipes, axe));
		return recipes;
	}

	private static void process(Item item, List<RecipeHolder<ManualApplicationRecipe>> list, ItemStack axe) {
		if (!(item instanceof BlockItem blockItem))
			return;
		BlockState state = blockItem.getBlock()
			.defaultBlockState();
		BlockState strippedState = AxeItem.getAxeStrippingState(state);
		if (strippedState == null)
			return;
		Item resultItem = strippedState.getBlock()
			.asItem();
		if (resultItem == null)
			return;
		list.add(create(item, resultItem, axe));
	}

	private static RecipeHolder<ManualApplicationRecipe> create(Item fromItem, Item toItem, ItemStack axe) {
		ResourceLocation rn = RegisteredObjectsHelper.getKeyOrThrow(toItem);
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(rn.getNamespace(), rn.getPath() + "_via_vanilla_stripping");
		ManualApplicationRecipe recipe = new ItemApplicationRecipe.Builder<>(ManualApplicationRecipe::new, id)
				.require(fromItem)
				.require(Ingredient.of(axe))
				.output(toItem)
				.build();

		return new RecipeHolder<>(id, recipe);
	}

}
