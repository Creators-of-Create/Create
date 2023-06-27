package com.simibubi.create.foundation.data.recipe;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

/**
 * Just in case players don't know about that vanilla feature
 */
public class LogStrippingFakeRecipes {

	public static List<ManualApplicationRecipe> createRecipes() {
		List<ManualApplicationRecipe> recipes = new ArrayList<>();
		if (!AllConfigs.server().recipes.displayLogStrippingRecipes.get())
			return recipes;

		ItemStack axe = new ItemStack(Items.IRON_AXE);
		axe.hideTooltipPart(TooltipPart.MODIFIERS);
		axe.setHoverName(Lang.translateDirect("recipe.item_application.any_axe")
			.withStyle(style -> style.withItalic(false)));
		ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
		tags.getTag(ItemTags.LOGS)
			.forEach(stack -> process(stack, recipes, axe));
		return recipes;
	}

	private static void process(Item item, List<ManualApplicationRecipe> list, ItemStack axe) {
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

	private static ManualApplicationRecipe create(Item fromItem, Item toItem, ItemStack axe) {
		ResourceLocation rn = RegisteredObjects.getKeyOrThrow(toItem);
		return new ProcessingRecipeBuilder<>(ManualApplicationRecipe::new,
			new ResourceLocation(rn.getNamespace(), rn.getPath() + "_via_vanilla_stripping")).require(fromItem)
				.require(Ingredient.of(axe))
				.output(toItem)
				.build();
	}

}
