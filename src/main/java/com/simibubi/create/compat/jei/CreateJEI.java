package com.simibubi.create.compat.jei;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.*;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateScreen;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.*;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@JeiPlugin
public class CreateJEI implements IModPlugin {

	private static final ResourceLocation ID = new ResourceLocation(Create.ID, "jei_plugin");

	public static final IIngredientType<FluidIngredient> FLUIDS = new IIngredientType<FluidIngredient>() {

		@Override
		public Class<? extends FluidIngredient> getIngredientClass() {
			return FluidIngredient.class;
		}
	};

	private final MillingCategory millingCategory;
	private final CrushingCategory crushingCategory;
	private final SplashingCategory splashingCategory;
	private final SmokingViaFanCategory smokingCategory;
	private final PressingCategory pressingCategory;
	private final BlastingViaFanCategory blastingCategory;
	private final BlockzapperUpgradeCategory blockzapperCategory;
	private final MixingCategory shapelessMixingCategory;
	private final MixingCategory mixingCategory;
	private final SawingCategory sawingCategory;
	private final BlockCuttingCategory blockCuttingCategory;
	private final PackingCategory packingCategory;
	private final PolishingCategory polishingCategory;
	private final MysteriousItemConversionCategory mysteryConversionCategory;
	private final MechanicalCraftingCategory mechanicalCraftingCategory;
	private final MechanicalCraftingCategory mechanicalCraftingExclusiveCategory;

	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	public CreateJEI() {
		millingCategory = new MillingCategory();
		crushingCategory = new CrushingCategory();
		splashingCategory = new SplashingCategory();
		pressingCategory = new PressingCategory();
		smokingCategory = new SmokingViaFanCategory();
		blastingCategory = new BlastingViaFanCategory();
		blockzapperCategory = new BlockzapperUpgradeCategory();
		shapelessMixingCategory = new MixingCategory(true);
		mixingCategory = new MixingCategory(false);
		sawingCategory = new SawingCategory();
		blockCuttingCategory = new BlockCuttingCategory();
		packingCategory = new PackingCategory();
		polishingCategory = new PolishingCategory();
		mysteryConversionCategory = new MysteriousItemConversionCategory();
		mechanicalCraftingCategory = new MechanicalCraftingCategory(true);
		mechanicalCraftingExclusiveCategory = new MechanicalCraftingCategory(false);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(AllItems.BLOCKZAPPER.get());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(millingCategory, crushingCategory, splashingCategory, pressingCategory, smokingCategory, blastingCategory, blockzapperCategory, mixingCategory, shapelessMixingCategory, sawingCategory, blockCuttingCategory, packingCategory, polishingCategory, mysteryConversionCategory, mechanicalCraftingCategory, mechanicalCraftingExclusiveCategory);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		CRecipes recipeConfig = AllConfigs.SERVER.recipes;

		registration.addRecipes(findRecipes(AllRecipeTypes.MILLING), millingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.CRUSHING), crushingCategory.getUid());
		registration.addRecipes(findRecipesByTypeExcluding(AllRecipeTypes.MILLING.getType(), AllRecipeTypes.CRUSHING.getType()), crushingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.SPLASHING), splashingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.PRESSING), pressingCategory.getUid());
		registration.addRecipes(findRecipesById(AllRecipeTypes.BLOCKZAPPER_UPGRADE.serializer.getRegistryName()), blockzapperCategory.getUid());
		registration.addRecipes(findRecipesByType(IRecipeType.SMOKING), smokingCategory.getUid());
		registration.addRecipes(findRecipesByTypeExcluding(IRecipeType.SMELTING, IRecipeType.SMOKING), blastingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.MIXING), mixingCategory.getUid());

		if (recipeConfig.allowShapelessInMixer.get())
			registration.addRecipes(
					findRecipes(r -> r.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS && !MechanicalPressTileEntity.canCompress(r.getIngredients()))
							.stream()
							.map(BasinRecipe::convert)
							.collect(Collectors.toList()),
					shapelessMixingCategory.getUid());

		registration.addRecipes(findRecipes(AllRecipeTypes.CUTTING), sawingCategory.getUid());

		if (recipeConfig.allowStonecuttingOnSaw.get())
			registration.addRecipes(CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(IRecipeType.STONECUTTING)), blockCuttingCategory.getUid());

		registration.addRecipes(findRecipes(AllRecipeTypes.COMPACTING), packingCategory.getUid());

		if (recipeConfig.allowShapedSquareInPress.get())
			registration.addRecipes(
					findRecipes(r -> (r instanceof ICraftingRecipe) && MechanicalPressTileEntity.canCompress(r.getIngredients()))
							.stream()
							.map(BasinRecipe::convert)
							.collect(Collectors.toList()),
					packingCategory.getUid());

		registration.addRecipes(findRecipes(AllRecipeTypes.SANDPAPER_POLISHING), polishingCategory.getUid());
		registration.addRecipes(MysteriousItemConversionCategory.getRecipes(), mysteryConversionCategory.getUid());
		registration.addRecipes(findRecipes(r -> (r.getType() == AllRecipeTypes.MECHANICAL_CRAFTING.type)), mechanicalCraftingExclusiveCategory.getUid());

		if (recipeConfig.allowRegularCraftingInCrafter.get())
			registration.addRecipes(
					findRecipes(r -> (r.getType() == IRecipeType.CRAFTING && r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.type) && (r instanceof ShapedRecipe)),
					mechanicalCraftingCategory.getUid());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		ItemStack fan = new ItemStack(AllBlocks.ENCASED_FAN.get());

		ItemStack splashingFan = fan.copy().setDisplayName(Lang.translate("recipe.splashing.fan").formatted(TextFormatting.RESET));
		ItemStack smokingFan = fan.copy().setDisplayName(Lang.translate("recipe.smokingViaFan.fan").formatted(TextFormatting.RESET));
		ItemStack blastingFan = fan.copy().setDisplayName(Lang.translate("recipe.blastingViaFan.fan").formatted(TextFormatting.RESET));

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MILLSTONE.get()), millingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.CRUSHING_WHEEL.get()), crushingCategory.getUid());

		registration.addRecipeCatalyst(splashingFan, splashingCategory.getUid());

		registration.addRecipeCatalyst(smokingFan, smokingCategory.getUid());

		registration.addRecipeCatalyst(blastingFan, blastingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_PRESS.get()), pressingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllItems.BLOCKZAPPER.get()), blockzapperCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_MIXER.get()), shapelessMixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), shapelessMixingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_MIXER.get()), mixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), mixingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_SAW.get()), sawingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_SAW.get()), blockCuttingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(Blocks.STONECUTTER), blockCuttingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_PRESS.get()), packingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), packingCategory.getUid());

		registration.addRecipeCatalyst(AllItems.SAND_PAPER.asStack(), polishingCategory.getUid());
		registration.addRecipeCatalyst(AllItems.RED_SAND_PAPER.asStack(), polishingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_CRAFTER.get()), mechanicalCraftingCategory.getUid());

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_CRAFTER.get()), mechanicalCraftingExclusiveCategory.getUid());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(AdjustableCrateScreen.class, new SlotMover<>());
		registration.addGuiContainerHandler(SchematicannonScreen.class, new SlotMover<>());
	}

	private static List<IRecipe<?>> findRecipes(AllRecipeTypes recipe) {
		return findRecipesByType(recipe.type);
	}

	private static List<IRecipe<?>> findRecipes(Predicate<IRecipe<?>> predicate) {
		return Minecraft.getInstance().world.getRecipeManager()
				.getRecipes()
				.stream()
				.filter(predicate)
				.collect(Collectors.toList());
	}

	private static List<IRecipe<?>> findRecipesByType(IRecipeType<?> type) {
		return findRecipes(r -> r.getType() == type);
	}

	private static List<IRecipe<?>> findRecipesById(ResourceLocation id) {
		return findRecipes(r -> r.getSerializer().getRegistryName().equals(id));
	}

	private static List<IRecipe<?>> findRecipesByTypeExcluding(IRecipeType<?> type, IRecipeType<?> excludingType) {
		List<IRecipe<?>> byType = findRecipesByType(type);
		List<IRecipe<?>> byExcludingType = findRecipesByType(excludingType);
		byType.removeIf(recipe -> {
			for (IRecipe<?> r : byExcludingType) {
				ItemStack[] matchingStacks = recipe.getIngredients().get(0).getMatchingStacks();
				if (matchingStacks.length == 0) return true;
				if (r.getIngredients().get(0).test(matchingStacks[0])) return true;
			}
			return false;
		});
		return byType;
	}

}
