package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CrushingCategory;
import com.simibubi.create.compat.jei.category.DeployingCategory;
import com.simibubi.create.compat.jei.category.FanBlastingCategory;
import com.simibubi.create.compat.jei.category.FanHauntingCategory;
import com.simibubi.create.compat.jei.category.FanSmokingCategory;
import com.simibubi.create.compat.jei.category.FanWashingCategory;
import com.simibubi.create.compat.jei.category.ItemApplicationCategory;
import com.simibubi.create.compat.jei.category.ItemDrainCategory;
import com.simibubi.create.compat.jei.category.MechanicalCraftingCategory;
import com.simibubi.create.compat.jei.category.MillingCategory;
import com.simibubi.create.compat.jei.category.MixingCategory;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;
import com.simibubi.create.compat.jei.category.PackingCategory;
import com.simibubi.create.compat.jei.category.PolishingCategory;
import com.simibubi.create.compat.jei.category.PressingCategory;
import com.simibubi.create.compat.jei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.jei.category.SawingCategory;
import com.simibubi.create.compat.jei.category.SequencedAssemblyCategory;
import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipes;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.content.logistics.item.LinkedControllerScreen;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.data.recipe.LogStrippingFakeRecipes;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.ModList;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CreateJEI implements IModPlugin {

	private static final ResourceLocation ID = Create.asResource("jei_plugin");

	public IIngredientManager ingredientManager;
	private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

	private void loadCategories() {
		allCategories.clear();
		CreateRecipeCategory<?>

				milling = register("milling", MillingCategory::new)
						.addTypedRecipes(AllRecipeTypes.MILLING)
						.catalyst(AllBlocks.MILLSTONE::get)
						.build(),

				crushing = register("crushing", CrushingCategory::new)
						.addTypedRecipes(AllRecipeTypes.CRUSHING)
						.addTypedRecipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
						.catalyst(AllBlocks.CRUSHING_WHEEL::get)
						.build(),

				pressing = register("pressing", PressingCategory::new)
						.addTypedRecipes(AllRecipeTypes.PRESSING)
						.catalyst(AllBlocks.MECHANICAL_PRESS::get)
						.build(),

				washing = register("fan_washing", FanWashingCategory::new)
						.addTypedRecipes(AllRecipeTypes.SPLASHING)
						.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
						.build(),

				smoking = register("fan_smoking", FanSmokingCategory::new)
						.addTypedRecipes(() -> RecipeType.SMOKING)
						.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
						.build(),

				blasting = register("fan_blasting", FanBlastingCategory::new)
						.addTypedRecipesExcluding(() -> RecipeType.SMELTING, () -> RecipeType.BLASTING)
						.addTypedRecipes(() -> RecipeType.BLASTING)
						.removeRecipes(() -> RecipeType.SMOKING)
						.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
						.build(),

				haunting = register("fan_haunting", FanHauntingCategory::new).addTypedRecipes(AllRecipeTypes.HAUNTING)
						.catalystStack(ProcessingViaFanCategory.getFan("fan_haunting"))
						.build(),

				mixing = register("mixing", MixingCategory::standard).addTypedRecipes(AllRecipeTypes.MIXING)
						.catalyst(AllBlocks.MECHANICAL_MIXER::get)
						.catalyst(AllBlocks.BASIN::get)
						.build(),

				seqAssembly = register("sequenced_assembly", SequencedAssemblyCategory::new)
						.addTypedRecipes(AllRecipeTypes.SEQUENCED_ASSEMBLY)
						.build(),

				autoShapeless = register("automatic_shapeless", MixingCategory::autoShapeless)
						.addAllRecipesIf(r -> r instanceof CraftingRecipe && !(r instanceof IShapedRecipe<?>)
										&& r.getIngredients()
										.size() > 1
										&& !MechanicalPressTileEntity.canCompress(r) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
								BasinRecipe::convertShapeless)
						.catalyst(AllBlocks.MECHANICAL_MIXER::get)
						.catalyst(AllBlocks.BASIN::get)
						.enableWhen(c -> c.allowShapelessInMixer)
						.build(),

				brewing = register("automatic_brewing", MixingCategory::autoBrewing).addRecipes(() -> PotionMixingRecipes.ALL)
						.catalyst(AllBlocks.MECHANICAL_MIXER::get)
						.catalyst(AllBlocks.BASIN::get)
						.build(),

				sawing = register("sawing", SawingCategory::new).addTypedRecipes(AllRecipeTypes.CUTTING)
						.catalyst(AllBlocks.MECHANICAL_SAW::get)
						.build(),

				blockCutting = register("block_cutting", () -> new BlockCuttingCategory(Items.STONE_BRICK_STAIRS))
						.addRecipes(() -> CondensedBlockCuttingRecipe.condenseRecipes(getTypedRecipesExcluding(RecipeType.STONECUTTING, AllRecipeTypes::shouldIgnoreInAutomation)))
						.catalyst(AllBlocks.MECHANICAL_SAW::get)
						.enableWhen(c -> c.allowStonecuttingOnSaw)
						.build(),

				woodCutting = register("wood_cutting", () -> new BlockCuttingCategory(Items.OAK_STAIRS))
						.addRecipes(() -> CondensedBlockCuttingRecipe.condenseRecipes(getTypedRecipesExcluding(SawTileEntity.woodcuttingRecipeType.get(), AllRecipeTypes::shouldIgnoreInAutomation)))
						.catalyst(AllBlocks.MECHANICAL_SAW::get)
						.enableWhenBool(c -> c.allowWoodcuttingOnSaw.get() && ModList.get()
								.isLoaded("druidcraft"))
						.build(),

				packing = register("packing", PackingCategory::standard).addTypedRecipes(AllRecipeTypes.COMPACTING)
						.catalyst(AllBlocks.MECHANICAL_PRESS::get)
						.catalyst(AllBlocks.BASIN::get)
						.build(),

				autoSquare = register("automatic_packing", PackingCategory::autoSquare)
						.addAllRecipesIf(
								r -> (r instanceof CraftingRecipe) && !(r instanceof MechanicalCraftingRecipe)
										&& MechanicalPressTileEntity.canCompress(r) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
								BasinRecipe::convertShapeless)
						.catalyst(AllBlocks.MECHANICAL_PRESS::get)
						.catalyst(AllBlocks.BASIN::get)
						.enableWhen(c -> c.allowShapedSquareInPress)
						.build(),

				polishing = register("sandpaper_polishing", PolishingCategory::new)
						.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING)
						.catalyst(AllItems.SAND_PAPER::get)
						.catalyst(AllItems.RED_SAND_PAPER::get)
						.build(),

				item_application = register("item_application", ItemApplicationCategory::new)
						.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION)
						.addRecipes(LogStrippingFakeRecipes::createRecipes)
						.build(),

				deploying = register("deploying", DeployingCategory::new).addTypedRecipes(AllRecipeTypes.DEPLOYING)
						.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING::getType, DeployerApplicationRecipe::convert)
						.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION::getType, ManualApplicationRecipe::asDeploying)
						.catalyst(AllBlocks.DEPLOYER::get)
						.catalyst(AllBlocks.DEPOT::get)
						.catalyst(AllItems.BELT_CONNECTOR::get)
						.build(),

				mysteryConversion = register("mystery_conversion", MysteriousItemConversionCategory::new)
						.addRecipes(() -> MysteriousItemConversionCategory.RECIPES)
						.build(),

				spoutFilling = register("spout_filling", SpoutCategory::new).addTypedRecipes(AllRecipeTypes.FILLING)
						.addRecipeListConsumer(recipes -> SpoutCategory.consumeRecipes(recipes::add, ingredientManager))
						.catalyst(AllBlocks.SPOUT::get)
						.build(),

				draining = register("draining", ItemDrainCategory::new)
						.addRecipeListConsumer(recipes -> ItemDrainCategory.consumeRecipes(recipes::add, ingredientManager))
						.addTypedRecipes(AllRecipeTypes.EMPTYING)
						.catalyst(AllBlocks.ITEM_DRAIN::get)
						.build(),

				autoShaped = register("automatic_shaped", MechanicalCraftingCategory::new)
						.addAllRecipesIf(r -> r instanceof CraftingRecipe && !(r instanceof IShapedRecipe<?>)
								&& r.getIngredients()
								.size() == 1
								&& !AllRecipeTypes.shouldIgnoreInAutomation(r))
						.addTypedRecipesIf(() -> RecipeType.CRAFTING,
								recipe -> recipe instanceof IShapedRecipe<?> && !AllRecipeTypes.shouldIgnoreInAutomation(recipe))
						.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
						.enableWhen(c -> c.allowRegularCraftingInCrafter)
						.build(),

				mechanicalCrafting = register("mechanical_crafting", MechanicalCraftingCategory::new)
						.addTypedRecipes(AllRecipeTypes.MECHANICAL_CRAFTING)
						.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
						.build();

	}

	private <T extends Recipe<?>> CategoryBuilder<T> register(String name, Supplier<CreateRecipeCategory<T>> supplier) {
		return new CategoryBuilder<>(name, supplier);
	}

	@Override
	@Nonnull
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(new BlueprintTransferHandler(), RecipeTypes.CRAFTING);
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		loadCategories();
		registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
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

		allCategories.forEach(c -> c.registerRecipes(registration));

		registration.addRecipes(RecipeTypes.CRAFTING, ToolboxColoringRecipeMaker.createRecipes().toList());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		allCategories.forEach(c -> c.recipeCatalysts.forEach(s -> registration.addRecipeCatalyst(s.get(), c.getRecipeType())));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());

		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(ScheduleScreen.class, new GhostIngredientHandler());
	}

	private class CategoryBuilder<T extends Recipe<?>> {
		private final CreateRecipeCategory<T> category;
		private final List<Consumer<List<T>>> recipeListConsumers = new ArrayList<>();
		private Predicate<CRecipes> predicate;

		public CategoryBuilder(String name, Supplier<CreateRecipeCategory<T>> category) {
			this.category = category.get();
			this.category.setCategoryId(name);
			mezz.jei.api.recipe.RecipeType<T> recipeType = this.category.getRecipeType();
			predicate = cRecipes -> true;
		}

		public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<T>> consumer) {
			recipeListConsumers.add(consumer);
			return this;
		}

		public CategoryBuilder<T> addRecipes(Supplier<Collection<? extends T>> collection) {
			return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
		}

		public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
			return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add((T) recipe);
				}
			}));
		}

		public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
			return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(converter.apply(recipe));
				}
			}));
		}

		public CategoryBuilder<T> addTypedRecipes(IRecipeTypeInfo recipeTypeEntry) {
			return addTypedRecipes(recipeTypeEntry::getType);
		}

		public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType) {
			return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipes::add, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType, Function<Recipe<?>, T> converter) {
			return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> recipes.add(converter.apply(recipe)), recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType, Predicate<Recipe<?>> pred) {
			return addRecipeListConsumer(recipes -> CreateJEI.<T>consumeTypedRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(recipe);
				}
			}, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
			Supplier<RecipeType<? extends T>> excluded) {
			return addRecipeListConsumer(recipes -> {
				List<Recipe<?>> excludedRecipes = getTypedRecipes(excluded.get());
				CreateJEI.<T>consumeTypedRecipes(recipe -> {
					for (Recipe<?> excludedRecipe : excludedRecipes) {
						if (doInputsMatch(recipe, excludedRecipe)) {
							return;
						}
					}
					recipes.add(recipe);
				}, recipeType.get());
			});
		}

		public CategoryBuilder<T> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
			return addRecipeListConsumer(recipes -> {
				List<Recipe<?>> excludedRecipes = getTypedRecipes(recipeType.get());
				recipes.removeIf(recipe -> {
					for (Recipe<?> excludedRecipe : excludedRecipes) {
						if (doInputsMatch(recipe, excludedRecipe)) {
							return true;
						}
					}
					return false;
				});
			});
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
			predicate = c -> configValue.apply(c).get();
			return this;
		}

		public CategoryBuilder<T> enableWhenBool(Function<CRecipes, Boolean> configValue) {
			predicate = configValue::apply;
			return this;
		}

		public CreateRecipeCategory<T> build() {
			if (predicate.test(AllConfigs.SERVER.recipes))
				category.recipes.add(() -> {
					List<T> recipes = new ArrayList<>();
					for (Consumer<List<T>> consumer : recipeListConsumers)
						consumer.accept(recipes);
					return recipes;
				});
			allCategories.add(category);
			return category;
		}

	}

	public static void consumeAllRecipes(Consumer<Recipe<?>> consumer) {
		Minecraft.getInstance()
			.getConnection()
			.getRecipeManager()
			.getRecipes()
			.forEach(consumer);
	}

	public static <T extends Recipe<?>> void consumeTypedRecipes(Consumer<T> consumer, RecipeType<?> type) {
		Map<ResourceLocation, Recipe<?>> map = Minecraft.getInstance()
			.getConnection()
			.getRecipeManager().recipes.get(type);
		if (map != null) {
			map.values().forEach(recipe -> consumer.accept((T) recipe));
		}
	}

	public static List<Recipe<?>> getTypedRecipes(RecipeType<?> type) {
		List<Recipe<?>> recipes = new ArrayList<>();
		consumeTypedRecipes(recipes::add, type);
		return recipes;
	}

	public static List<Recipe<?>> getTypedRecipesExcluding(RecipeType<?> type, Predicate<Recipe<?>> exclusionPred) {
		List<Recipe<?>> recipes = getTypedRecipes(type);
		recipes.removeIf(exclusionPred);
		return recipes;
	}

	public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		if (recipe1.getIngredients()
			.isEmpty()
			|| recipe2.getIngredients()
				.isEmpty()) {
			return false;
		}
		ItemStack[] matchingStacks = recipe1.getIngredients()
			.get(0)
			.getItems();
		if (matchingStacks.length == 0) {
			return false;
		}
		return recipe2.getIngredients()
				.get(0)
				.test(matchingStacks[0]);
	}

}
