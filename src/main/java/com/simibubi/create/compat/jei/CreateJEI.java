package com.simibubi.create.compat.jei;

import java.util.List;
import java.util.stream.Collectors;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipes;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.Lang;

import com.simibubi.create.modules.logistics.block.FlexcrateScreen;
import com.simibubi.create.modules.schematics.block.SchematicannonScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

@JeiPlugin
public class CreateJEI implements IModPlugin {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "jei_plugin");
	private CrushingCategory crushingCategory;
	private SplashingCategory splashingCategory;
	private SmokingViaFanCategory smokingCategory;
	private PressingCategory pressingCategory;
	private BlastingViaFanCategory blastingCategory;
	private BlockzapperUpgradeCategory blockzapperCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	public CreateJEI() {
		crushingCategory = new CrushingCategory();
		splashingCategory = new SplashingCategory();
		pressingCategory = new PressingCategory();
		smokingCategory = new SmokingViaFanCategory();
		blastingCategory = new BlastingViaFanCategory();
		blockzapperCategory = new BlockzapperUpgradeCategory();
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(AllItems.PLACEMENT_HANDGUN.item);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(crushingCategory, splashingCategory, pressingCategory, smokingCategory,
				blastingCategory, blockzapperCategory);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(findRecipes(AllRecipes.CRUSHING), crushingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipes.SPLASHING), splashingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipes.PRESSING), pressingCategory.getUid());
		registration.addRecipes(findRecipesById(AllRecipes.BLOCKZAPPER_UPGRADE.serializer.getRegistryName()),
				blockzapperCategory.getUid());
		registration.addRecipes(findRecipesByType(IRecipeType.SMOKING), smokingCategory.getUid());
		registration.addRecipes(findRecipesByTypeExcluding(IRecipeType.SMELTING, IRecipeType.SMOKING),
				blastingCategory.getUid());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		ItemStack fan = new ItemStack(AllBlocks.ENCASED_FAN.get());

		ItemStack splashingFan = fan.copy()
				.setDisplayName(new StringTextComponent(TextFormatting.RESET + Lang.translate("recipe.splashing.fan")));
		ItemStack smokingFan = fan.copy().setDisplayName(
				new StringTextComponent(TextFormatting.RESET + Lang.translate("recipe.smokingViaFan.fan")));
		ItemStack blastingFan = fan.copy().setDisplayName(
				new StringTextComponent(TextFormatting.RESET + Lang.translate("recipe.blastingViaFan.fan")));

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.CRUSHING_WHEEL.get()), crushingCategory.getUid());
		registration.addRecipeCatalyst(splashingFan, splashingCategory.getUid());
		registration.addRecipeCatalyst(smokingFan, smokingCategory.getUid());
		registration.addRecipeCatalyst(blastingFan, blastingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_PRESS.get()), pressingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllItems.PLACEMENT_HANDGUN.get()), blockzapperCategory.getUid());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(FlexcrateScreen.class, new SlotMover<>());
		registration.addGuiContainerHandler(SchematicannonScreen.class, new SlotMover<>());
	}

	private static List<IRecipe<?>> findRecipes(AllRecipes recipe) {
		return findRecipesByType(recipe.type);
	}

	private static List<IRecipe<?>> findRecipesByType(IRecipeType<?> type) {
		return Minecraft.getInstance().world.getRecipeManager().getRecipes().stream().filter(r -> r.getType() == type)
				.collect(Collectors.toList());
	}

	private static List<IRecipe<?>> findRecipesById(ResourceLocation id) {
		return Minecraft.getInstance().world.getRecipeManager().getRecipes().stream()
				.filter(r -> r.getSerializer().getRegistryName().equals(id)).collect(Collectors.toList());
	}

	private static List<IRecipe<?>> findRecipesByTypeExcluding(IRecipeType<?> type, IRecipeType<?> excludingType) {
		List<IRecipe<?>> byType = findRecipesByType(type);
		List<IRecipe<?>> byExcludingType = findRecipesByType(excludingType);
		byType.removeIf(recipe -> {
			for (IRecipe<?> r : byExcludingType) {
				ItemStack[] matchingStacks = recipe.getIngredients().get(0).getMatchingStacks();
				if (matchingStacks.length == 0)
					return true;
				if (r.getIngredients().get(0).test(matchingStacks[0]))
					return true;
			}
			return false;
		});
		return byType;
	}

}
