package com.simibubi.create.compat.jei;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Predicate;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.contraptions.components.mixer.MixingRecipe;
import com.simibubi.create.modules.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.modules.contraptions.processing.ProcessingOutput;
import com.simibubi.create.modules.logistics.block.inventories.FlexcrateScreen;
import com.simibubi.create.modules.schematics.block.SchematicannonScreen;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
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
	private MixingCategory mixingCategory;
	private SawingCategory sawingCategory;
	private BlockCuttingCategory blockCuttingCategory;
	private PackingCategory packingCategory;

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
		mixingCategory = new MixingCategory();
		sawingCategory = new SawingCategory();
		blockCuttingCategory = new BlockCuttingCategory();
		packingCategory = new PackingCategory();
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(AllItems.PLACEMENT_HANDGUN.item);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(crushingCategory, splashingCategory, pressingCategory, smokingCategory,
				blastingCategory, blockzapperCategory, mixingCategory, sawingCategory, blockCuttingCategory,
				packingCategory);
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
		registration.addRecipes(findRecipes(AllRecipes.MIXING), mixingCategory.getUid());
		registration.addRecipes(findRecipes(r -> r.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS
				&& !MechanicalPressTileEntity.canCompress(r.getIngredients())).stream().map(MixingRecipe::of)
						.collect(Collectors.toList()),
				mixingCategory.getUid());
		registration.addRecipes(findRecipes(AllRecipes.CUTTING), sawingCategory.getUid());
		registration.addRecipes(
				CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(IRecipeType.STONECUTTING)),
				blockCuttingCategory.getUid());
		registration.addRecipes(findRecipes(
				r -> (r instanceof ICraftingRecipe) && MechanicalPressTileEntity.canCompress(r.getIngredients())),
				packingCategory.getUid());
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
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_MIXER.get()), mixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), mixingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.SAW.get()), sawingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.SAW.get()), blockCuttingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(Blocks.STONECUTTER), blockCuttingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.MECHANICAL_PRESS.get()), packingCategory.getUid());
		registration.addRecipeCatalyst(new ItemStack(AllBlocks.BASIN.get()), packingCategory.getUid());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(FlexcrateScreen.class, new SlotMover<>());
		registration.addGuiContainerHandler(SchematicannonScreen.class, new SlotMover<>());
	}

	private static List<IRecipe<?>> findRecipes(AllRecipes recipe) {
		return findRecipesByType(recipe.type);
	}

	private static List<IRecipe<?>> findRecipes(Predicate<IRecipe<?>> pred) {
		return Minecraft.getInstance().world.getRecipeManager().getRecipes().stream().filter(pred)
				.collect(Collectors.toList());
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

	static void addStochasticTooltip(IGuiItemStackGroup itemStacks, List<ProcessingOutput> results) {
		itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input)
				return;
			ProcessingOutput output = results.get(slotIndex - 1);
			if (output.getChance() != 1)
				tooltip.add(1, TextFormatting.GOLD
						+ Lang.translate("recipe.processing.chance", (int) (output.getChance() * 100)));
		});
	}

	static void addCatalystTooltip(IGuiItemStackGroup itemStacks, Map<Integer, Float> catalystIndices) {
		itemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (!input)
				return;
			if (!catalystIndices.containsKey(slotIndex))
				return;
			Float chance = catalystIndices.get(slotIndex);
			tooltip.add(1, TextFormatting.YELLOW + Lang.translate("recipe.processing.catalyst"));
			tooltip.add(2, TextFormatting.GOLD
					+ Lang.translate("recipe.processing.chanceToReturn", (int) (chance.floatValue() * 100)));
		});
	}

}
