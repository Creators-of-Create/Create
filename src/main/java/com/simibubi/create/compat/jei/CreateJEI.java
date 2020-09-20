package com.simibubi.create.compat.jei;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Predicate;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.BlastingViaFanCategory;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.BlockzapperUpgradeCategory;
import com.simibubi.create.compat.jei.category.CrushingCategory;
import com.simibubi.create.compat.jei.category.MechanicalCraftingCategory;
import com.simibubi.create.compat.jei.category.MillingCategory;
import com.simibubi.create.compat.jei.category.MixingCategory;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;
import com.simibubi.create.compat.jei.category.PackingCategory;
import com.simibubi.create.compat.jei.category.PolishingCategory;
import com.simibubi.create.compat.jei.category.PressingCategory;
import com.simibubi.create.compat.jei.category.SawingCategory;
import com.simibubi.create.compat.jei.category.SmokingViaFanCategory;
import com.simibubi.create.compat.jei.category.SplashingCategory;
import com.simibubi.create.content.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateScreen;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.simibubi.create.foundation.utility.Lang;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

@JeiPlugin
public class CreateJEI implements IModPlugin {

	private static ResourceLocation ID = new ResourceLocation(Create.ID, "jei_plugin");
	private MillingCategory millingCategory;
	private CrushingCategory crushingCategory;
	private SplashingCategory splashingCategory;
	private SmokingViaFanCategory smokingCategory;
	private PressingCategory pressingCategory;
	private BlastingViaFanCategory blastingCategory;
	private BlockzapperUpgradeCategory blockzapperCategory;
	private MixingCategory mixingCategory;
	private SawingCategory sawingCategory;
	private BlockCuttingCategory blockCuttingCategory;
	private PackingCategory packingCategory;
	private PolishingCategory polishingCategory;
	private MysteriousItemConversionCategory mysteryConversionCategory;
	private MechanicalCraftingCategory mechanicalCraftingCategory;

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
		mixingCategory = new MixingCategory();
		sawingCategory = new SawingCategory();
		blockCuttingCategory = new BlockCuttingCategory();
		packingCategory = new PackingCategory();
		polishingCategory = new PolishingCategory();
		mysteryConversionCategory = new MysteriousItemConversionCategory();
		mechanicalCraftingCategory = new MechanicalCraftingCategory();
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(AllItems.BLOCKZAPPER.get());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration
				.addRecipeCategories(millingCategory, crushingCategory, splashingCategory, pressingCategory,
						smokingCategory, blastingCategory, blockzapperCategory, mixingCategory, sawingCategory,
						blockCuttingCategory, packingCategory, polishingCategory, mysteryConversionCategory,
						mechanicalCraftingCategory);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(findRecipes(AllRecipeTypes.MILLING), millingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.CRUSHING), crushingCategory.getUid());
		registration.addRecipes(findRecipesByTypeExcluding(AllRecipeTypes.MILLING.getType(), AllRecipeTypes.CRUSHING.getType()),
				crushingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.SPLASHING), splashingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.PRESSING), pressingCategory.getUid());
		registration.addRecipes(findRecipesById(AllRecipeTypes.BLOCKZAPPER_UPGRADE.serializer.getRegistryName()),
				blockzapperCategory.getUid());
		registration.addRecipes(findRecipesByType(IRecipeType.SMOKING), smokingCategory.getUid());
		registration.addRecipes(findRecipesByTypeExcluding(IRecipeType.SMELTING, IRecipeType.SMOKING),
				blastingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.MIXING), mixingCategory.getUid());
		registration.addRecipes(findRecipes(r -> r.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS
				&& !MechanicalPressTileEntity.canCompress(r.getIngredients())).stream().map(MixingRecipe::convertShapeless)
						.collect(Collectors.toList()),
				mixingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.CUTTING), sawingCategory.getUid());
		registration.addRecipes(
				CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(IRecipeType.STONECUTTING)),
				blockCuttingCategory.getUid());
		registration.addRecipes(findRecipes(
				r -> (r instanceof ICraftingRecipe) && MechanicalPressTileEntity.canCompress(r.getIngredients())),
				packingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipeTypes.SANDPAPER_POLISHING), polishingCategory.getUid());
		registration.addRecipes(MysteriousItemConversionCategory.getRecipes(), mysteryConversionCategory.getUid());
		registration.addRecipes(findRecipes(r -> (r.getType() == AllRecipeTypes.MECHANICAL_CRAFTING.type)),
				mechanicalCraftingCategory.getUid());
		registration.addRecipes(findRecipes(r -> (r.getType() == IRecipeType.CRAFTING 
				&& r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.type) && (r instanceof ShapedRecipe)),
				mechanicalCraftingCategory.getUid());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		ItemStack fan = new ItemStack(AllBlocks.ENCASED_FAN.get());

		ItemStack splashingFan = fan
				.copy()
				.setDisplayName(new StringTextComponent(TextFormatting.RESET + Lang.translate("recipe.splashing.fan")));
		ItemStack smokingFan = fan
				.copy()
				.setDisplayName(
						new StringTextComponent(TextFormatting.RESET + Lang.translate("recipe.smokingViaFan.fan")));
		ItemStack blastingFan = fan
				.copy()
				.setDisplayName(
						new StringTextComponent(TextFormatting.RESET + Lang.translate("recipe.blastingViaFan.fan")));

		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MILLSTONE.get()), millingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.CRUSHING_WHEEL.get()), crushingCategory.getUid());
		registration.addRecipeCatalyst(splashingFan, splashingCategory.getUid());
		registration.addRecipeCatalyst(smokingFan, smokingCategory.getUid());
		registration.addRecipeCatalyst(blastingFan, blastingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_PRESS.get()), pressingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllItems.BLOCKZAPPER.get()), blockzapperCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_MIXER.get()), mixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), mixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BLAZE_BURNER.get()), mixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_SAW.get()), sawingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_SAW.get()), blockCuttingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(Blocks.STONECUTTER), blockCuttingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_PRESS.get()), packingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), packingCategory.getUid());
		registration.addRecipeCatalyst(AllItems.SAND_PAPER.asStack(), polishingCategory.getUid());
		registration.addRecipeCatalyst(AllItems.RED_SAND_PAPER.asStack(), polishingCategory.getUid());
		registration
				.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_CRAFTER.get()),
						mechanicalCraftingCategory.getUid());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(AdjustableCrateScreen.class, new SlotMover<>());
		registration.addGuiContainerHandler(SchematicannonScreen.class, new SlotMover<>());
	}

	private static List<IRecipe<?>> findRecipes(AllRecipeTypes recipe) {
		return findRecipesByType(recipe.type);
	}

	private static List<IRecipe<?>> findRecipes(Predicate<IRecipe<?>> pred) {
		return Minecraft.getInstance().world
				.getRecipeManager()
				.getRecipes()
				.stream()
				.filter(pred)
				.collect(Collectors.toList());
	}

	private static List<IRecipe<?>> findRecipesByType(IRecipeType<?> type) {
		return Minecraft.getInstance().world
				.getRecipeManager()
				.getRecipes()
				.stream()
				.filter(r -> r.getType() == type)
				.collect(Collectors.toList());
	}

	private static List<IRecipe<?>> findRecipesById(ResourceLocation id) {
		return Minecraft.getInstance().world
				.getRecipeManager()
				.getRecipes()
				.stream()
				.filter(r -> r.getSerializer().getRegistryName().equals(id))
				.collect(Collectors.toList());
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
