package com.simibubi.create.compat.jei;

import com.google.common.base.Predicates;
import com.simibubi.create.*;
import com.simibubi.create.compat.jei.category.*;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.content.logistics.item.LinkedControllerScreen;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@JeiPlugin
@SuppressWarnings("unused")
public class CreateJEI implements IModPlugin {

	private static final ResourceLocation ID = Create.asResource("jei_plugin");

	public IIngredientManager ingredientManager;
	private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
	private final CreateRecipeCategory<?>

			milling = register("milling", MillingCategory::new).recipes(AllRecipeTypes.MILLING)
			.catalyst(AllBlocks.MILLSTONE::get)
			.build(),

	crushing = register("crushing", CrushingCategory::new).recipes(AllRecipeTypes.CRUSHING)
			.recipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
			.catalyst(AllBlocks.CRUSHING_WHEEL::get)
			.build(),

	pressing = register("pressing", PressingCategory::new).recipes(AllRecipeTypes.PRESSING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.build(),

	washing = register("fan_washing", FanWashingCategory::new).recipes(AllRecipeTypes.SPLASHING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
			.build(),

	smoking = register("fan_smoking", FanSmokingCategory::new).recipes(() -> RecipeType.SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
			.build(),

	soul_smoking = register("fan_soul_smoking", FanSoulSmokingCategory::new).recipes(AllRecipeTypes.SOUL_SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_soul_smoking")).build(),

	blasting = register("fan_blasting", FanBlastingCategory::new)
			.recipesExcluding(() -> RecipeType.SMELTING, () -> RecipeType.BLASTING)
			.recipes(() -> RecipeType.BLASTING)
			.removeRecipes(() -> RecipeType.SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
			.build(),

	mixing = register("mixing", MixingCategory::standard).recipes(AllRecipeTypes.MIXING::getType)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

	seqAssembly = register("sequenced_assembly", SequencedAssemblyCategory::new)
			.recipes(AllRecipeTypes.SEQUENCED_ASSEMBLY::getType)
			.build(),

	autoShapeless = register("automatic_shapeless", MixingCategory::autoShapeless)
			.recipes(r -> r.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE && r.getIngredients()
							.size() > 1 && !MechanicalPressTileEntity.canCompress(r) && !AllRecipeTypes.isManualRecipe(r),
					BasinRecipe::convertShapeless)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> c.allowShapelessInMixer)
			.build(),

	brewing = register("automatic_brewing", MixingCategory::autoBrewing)
			.recipeList(PotionMixingRecipeManager::getAllBrewingRecipes)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

	sawing = register("sawing", SawingCategory::new).recipes(AllRecipeTypes.CUTTING)
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.build(),

	blockCutting = register("block_cutting", () -> new BlockCuttingCategory(Items.STONE_BRICK_STAIRS))
			.recipeList(() -> CondensedBlockCuttingRecipe.condenseRecipes(findRecipes(
					recipe -> recipe.getType() == RecipeType.STONECUTTING && !AllRecipeTypes.isManualRecipe(recipe))))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhen(c -> c.allowStonecuttingOnSaw)
			.build(),

	woodCutting = register("wood_cutting", () -> new BlockCuttingCategory(Items.OAK_STAIRS))
			.recipeList(() -> CondensedBlockCuttingRecipe
					.condenseRecipes(findRecipes(recipe -> recipe.getType() == SawTileEntity.woodcuttingRecipeType.get()
							&& !AllRecipeTypes.isManualRecipe(recipe))))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhenBool(c -> c.allowWoodcuttingOnSaw.get() && ModList.get()
					.isLoaded("druidcraft"))
			.build(),

	packing = register("packing", PackingCategory::standard).recipes(AllRecipeTypes.COMPACTING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

	autoSquare = register("automatic_packing", PackingCategory::autoSquare)
			.recipes(
					r -> (r instanceof CraftingRecipe) && !(r instanceof MechanicalCraftingRecipe)
							&& MechanicalPressTileEntity.canCompress(r) && !AllRecipeTypes.isManualRecipe(r),
					BasinRecipe::convertShapeless)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> c.allowShapedSquareInPress)
			.build(),

	polishing = register("sandpaper_polishing", PolishingCategory::new).recipes(AllRecipeTypes.SANDPAPER_POLISHING)
			.catalyst(AllItems.SAND_PAPER::get)
			.catalyst(AllItems.RED_SAND_PAPER::get)
			.build(),

	deploying = register("deploying", DeployingCategory::new)
			.recipeList(() -> DeployerApplicationRecipe
					.convert(findRecipesByType(AllRecipeTypes.SANDPAPER_POLISHING.getType())))
			.recipes(AllRecipeTypes.DEPLOYING)
			.catalyst(AllBlocks.DEPLOYER::get)
			.catalyst(AllBlocks.DEPOT::get)
			.catalyst(AllItems.BELT_CONNECTOR::get)
			.build(),

	mysteryConversion = register("mystery_conversion", MysteriousItemConversionCategory::new)
			.recipeList(MysteriousItemConversionCategory::getRecipes)
			.build(),

	spoutFilling = register("spout_filling", SpoutCategory::new).recipes(AllRecipeTypes.FILLING)
			.recipeList(() -> SpoutCategory.getRecipes(ingredientManager))
			.catalyst(AllBlocks.SPOUT::get)
			.build(),

	draining = register("draining", ItemDrainCategory::new)
			.recipeList(() -> ItemDrainCategory.getRecipes(ingredientManager))
			.recipes(AllRecipeTypes.EMPTYING)
			.catalyst(AllBlocks.ITEM_DRAIN::get)
			.build(),

	autoShaped = register("automatic_shaped", MechanicalCraftingCategory::new)
			.recipes(r -> r.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE && r.getIngredients()
					.size() == 1)
			.recipes(r -> (r.getType() == RecipeType.CRAFTING
					&& r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.getType()) && (r instanceof ShapedRecipe)
					&& !AllRecipeTypes.isManualRecipe(r))
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
			.enableWhen(c -> c.allowRegularCraftingInCrafter)
			.build(),

	mechanicalCrafting =
			register("mechanical_crafting", MechanicalCraftingCategory::new).recipes(AllRecipeTypes.MECHANICAL_CRAFTING)
					.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
					.build();

	private <T extends Recipe<?>> CategoryBuilder<T> register(String name,
															  Supplier<CreateRecipeCategory<T>> supplier) {
		return new CategoryBuilder<T>(name, supplier);
	}

	@Override
	@Nonnull
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(new BlueprintTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		allCategories.forEach(registration::addRecipeCategories);
	}

	@Override
	public void registerFluidSubtypes(ISubtypeRegistration registration) {
		PotionFluidSubtypeInterpreter interpreter = new PotionFluidSubtypeInterpreter();
		PotionFluid potionFluid = AllFluids.POTION.get();
		registration.registerSubtypeInterpreter(potionFluid.getSource(), interpreter);
		registration.registerSubtypeInterpreter(potionFluid.getFlowing(), interpreter);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ingredientManager = registration.getIngredientManager();
		allCategories.forEach(c -> c.recipes.forEach(s -> registration.addRecipes(s.get(), c.getUid())));

		registration.addRecipes(ToolboxColoringRecipeMaker.createRecipes()
				.collect(Collectors.toList()), VanillaRecipeCategoryUid.CRAFTING);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		allCategories.forEach(c -> c.recipeCatalysts.forEach(s -> registration.addRecipeCatalyst(s.get(), c.getUid())));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());

		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
	}

	private class CategoryBuilder<T extends Recipe<?>> {
		private CreateRecipeCategory<T> category;
		private List<Consumer<List<Recipe<?>>>> recipeListConsumers = new ArrayList<>();
		private Predicate<CRecipes> pred;

		public CategoryBuilder(String name, Supplier<CreateRecipeCategory<T>> category) {
			this.category = category.get();
			this.category.setCategoryId(name);
			pred = Predicates.alwaysTrue();
		}

		public CategoryBuilder<T> recipes(IRecipeTypeInfo recipeTypeEntry) {
			return recipes(recipeTypeEntry::getType);
		}

		public CategoryBuilder<T> recipes(Supplier<RecipeType<? extends T>> recipeType) {
			return recipes(r -> r.getType() == recipeType.get());
		}

		public CategoryBuilder<T> recipes(ResourceLocation serializer) {
			return recipes(r -> r.getSerializer()
					.getRegistryName()
					.equals(serializer));
		}

		public CategoryBuilder<T> recipes(Predicate<Recipe<?>> pred) {
			return recipeList(() -> findRecipes(pred));
		}

		public CategoryBuilder<T> recipes(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
			return recipeList(() -> findRecipes(pred), converter);
		}

		public CategoryBuilder<T> recipeList(Supplier<List<? extends Recipe<?>>> list) {
			return recipeList(list, null);
		}

		public CategoryBuilder<T> recipeList(Supplier<List<? extends Recipe<?>>> list,
											 Function<Recipe<?>, T> converter) {
			recipeListConsumers.add(recipes -> {
				List<? extends Recipe<?>> toAdd = list.get();
				if (converter != null)
					toAdd = toAdd.stream()
							.map(converter)
							.collect(Collectors.toList());
				recipes.addAll(toAdd);
			});
			return this;
		}

		public CategoryBuilder<T> recipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
												   Supplier<RecipeType<? extends T>> excluded) {
			recipeListConsumers.add(recipes -> {
				recipes.addAll(findRecipesByTypeExcluding(recipeType.get(), excluded.get()));
			});
			return this;
		}

		public CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
			recipeListConsumers.add(recipes -> {
				removeRecipesByType(recipes, recipeType.get());
			});
			return this;
		}

		public CategoryBuilder<T> catalyst(Supplier<ItemLike> supplier) {
			return catalystStack(() -> new ItemStack(supplier.get()
					.asItem()));
		}

		public CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
			category.recipeCatalysts.add(supplier);
			return this;
		}

		public CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBool> configValue) {
			pred = c -> configValue.apply(c)
					.get();
			return this;
		}

		public CategoryBuilder<T> enableWhenBool(Function<CRecipes, Boolean> configValue) {
			pred = configValue::apply;
			return this;
		}

		public CreateRecipeCategory<T> build() {
			if (pred.test(AllConfigs.SERVER.recipes))
				category.recipes.add(() -> {
					List<Recipe<?>> recipes = new ArrayList<>();
					for (Consumer<List<Recipe<?>>> consumer : recipeListConsumers)
						consumer.accept(recipes);
					return recipes;
				});
			allCategories.add(category);
			return category;
		}

	}

	public static List<Recipe<?>> findRecipes(Predicate<Recipe<?>> predicate) {
		return Minecraft.getInstance()
			.getConnection()
			.getRecipeManager()
			.getRecipes()
			.stream()
			.filter(predicate)
			.collect(Collectors.toList());
	}

	public static List<Recipe<?>> findRecipesByType(RecipeType<?> type) {
		return findRecipes(recipe -> recipe.getType() == type);
	}

	public static List<Recipe<?>> findRecipesByTypeExcluding(RecipeType<?> type, RecipeType<?> excludingType) {
		List<Recipe<?>> byType = findRecipesByType(type);
		removeRecipesByType(byType, excludingType);
		return byType;
	}

	public static List<Recipe<?>> findRecipesByTypeExcluding(RecipeType<?> type, RecipeType<?>... excludingTypes) {
		List<Recipe<?>> byType = findRecipesByType(type);
		for (RecipeType<?> excludingType : excludingTypes)
			removeRecipesByType(byType, excludingType);
		return byType;
	}

	public static void removeRecipesByType(List<Recipe<?>> recipes, RecipeType<?> type) {
		List<Recipe<?>> byType = findRecipesByType(type);
		recipes.removeIf(recipe -> {
			for (Recipe<?> r : byType)
				if (doInputsMatch(recipe, r))
					return true;
			return false;
		});
	}

	public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		ItemStack[] matchingStacks = recipe1.getIngredients()
				.get(0)
				.getItems();
		if (matchingStacks.length == 0)
			return true;
		if (recipe2.getIngredients()
				.get(0)
				.test(matchingStacks[0]))
			return true;
		return false;
	}

}
