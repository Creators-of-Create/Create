package com.simibubi.create.compat.rei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.rei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.CrushingCategory;
import com.simibubi.create.compat.rei.category.DeployingCategory;
import com.simibubi.create.compat.rei.category.FanBlastingCategory;
import com.simibubi.create.compat.rei.category.FanHauntingCategory;
import com.simibubi.create.compat.rei.category.FanSmokingCategory;
import com.simibubi.create.compat.rei.category.FanWashingCategory;
import com.simibubi.create.compat.rei.category.ItemDrainCategory;
import com.simibubi.create.compat.rei.category.MechanicalCraftingCategory;
import com.simibubi.create.compat.rei.category.MillingCategory;
import com.simibubi.create.compat.rei.category.MixingCategory;
import com.simibubi.create.compat.rei.category.MysteriousItemConversionCategory;
import com.simibubi.create.compat.rei.category.PackingCategory;
import com.simibubi.create.compat.rei.category.PolishingCategory;
import com.simibubi.create.compat.rei.category.PressingCategory;
import com.simibubi.create.compat.rei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.rei.category.SawingCategory;
import com.simibubi.create.compat.rei.category.SequencedAssemblyCategory;
import com.simibubi.create.compat.rei.category.SpoutCategory;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipes;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class CreateREI implements REIClientPlugin {

	private static final ResourceLocation ID = Create.asResource("rei_plugin");

	private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();

	private void loadCategories() {
		allCategories.clear();
		CreateRecipeCategory<?>

		milling = register("milling", MillingCategory::new).addTypedRecipes(AllRecipeTypes.MILLING)
		.catalyst(AllBlocks.MILLSTONE::get)
		.build(),

			crushing = register("crushing", CrushingCategory::new).addTypedRecipes(AllRecipeTypes.CRUSHING)
				.addTypedRecipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
				.catalyst(AllBlocks.CRUSHING_WHEEL::get)
				.build(),

			pressing = register("pressing", PressingCategory::new).addTypedRecipes(AllRecipeTypes.PRESSING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.build(),

			washing = register("fan_washing", FanWashingCategory::new).addTypedRecipes(AllRecipeTypes.SPLASHING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
				.build(),

			smoking = register("fan_smoking", FanSmokingCategory::new).addTypedRecipes(() -> RecipeType.SMOKING)
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

			brewing =
				register("automatic_brewing", MixingCategory::autoBrewing).addRecipes(() -> PotionMixingRecipes.ALL)
					.catalyst(AllBlocks.MECHANICAL_MIXER::get)
					.catalyst(AllBlocks.BASIN::get)
					.build(),

			sawing = register("sawing", SawingCategory::new).addTypedRecipes(AllRecipeTypes.CUTTING)
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.build(),

			blockCutting =
				register("block_cutting", () -> new BlockCuttingCategory(Items.STONE_BRICK_STAIRS))
					.addRecipes(() -> CondensedBlockCuttingRecipe.condenseRecipes(getTypedRecipesExcluding(
						RecipeType.STONECUTTING, recipe -> AllRecipeTypes.shouldIgnoreInAutomation(recipe))))
					.catalyst(AllBlocks.MECHANICAL_SAW::get)
					.enableWhen(c -> c.allowStonecuttingOnSaw)
					.build(),

			woodCutting = register("wood_cutting", () -> new BlockCuttingCategory(Items.OAK_STAIRS))
				.addRecipes(() -> CondensedBlockCuttingRecipe
					.condenseRecipes(getTypedRecipesExcluding(SawTileEntity.woodcuttingRecipeType.get(),
						recipe -> AllRecipeTypes.shouldIgnoreInAutomation(recipe))))
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

			deploying = register("deploying", DeployingCategory::new).addTypedRecipes(AllRecipeTypes.DEPLOYING)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING::getType, DeployerApplicationRecipe::convert)
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
	public String getPluginProviderName() {
		return ID.toString();
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		allCategories.forEach(registry::add);
		allCategories.forEach(createRecipeCategory -> registry.removePlusButton(createRecipeCategory.getCategoryIdentifier()));
		allCategories.forEach(c -> c.recipeCatalysts.forEach(s -> registry.addWorkstations(c.getCategoryIdentifier(), EntryStack.of(VanillaEntryTypes.ITEM, s.get()))));
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		loadCategories();
		registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
	}

	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(AbstractSimiContainerScreen.class, new SlotMover());
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerDraggableStackVisitor(new GhostIngredientHandler<>());
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		registry.register(new BlueprintTransferHandler());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void registerEntries(EntryRegistry registry) {
		registry.removeEntryIf(entryStack -> {
			if(entryStack.getType() == VanillaEntryTypes.ITEM) {
				ItemStack itemStack = entryStack.castValue();
				if(itemStack.getItem() instanceof TagDependentIngredientItem tagItem) {
					return tagItem.shouldHide();
				}
			} else if(entryStack.getType() == VanillaEntryTypes.FLUID) {
				FluidStack fluidStack = entryStack.castValue();
				return fluidStack.getFluid() instanceof VirtualFluid;
			}
			return false;
		});
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

		public CategoryBuilder<T> addRecipeListConsumer(Consumer<List<Recipe<?>>> consumer) {
			recipeListConsumers.add(consumer);
			return this;
		}

		public CategoryBuilder<T> addRecipes(Supplier<Collection<? extends Recipe<?>>> collection) {
			return addRecipeListConsumer(recipes -> recipes.addAll(collection.get()));
		}

		public CategoryBuilder<T> addAllRecipesIf(Predicate<Recipe<?>> pred) {
			return addRecipeListConsumer(recipes -> consumeAllRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(recipe);
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
			return addRecipeListConsumer(recipes -> consumeTypedRecipes(recipes::add, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipes(Supplier<RecipeType<? extends T>> recipeType,
			Function<Recipe<?>, T> converter) {
			return addRecipeListConsumer(recipes -> consumeTypedRecipes(recipe -> {
				recipes.add(converter.apply(recipe));
			}, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipesIf(Supplier<RecipeType<? extends T>> recipeType,
			Predicate<Recipe<?>> pred) {
			return addRecipeListConsumer(recipes -> consumeTypedRecipes(recipe -> {
				if (pred.test(recipe)) {
					recipes.add(recipe);
				}
			}, recipeType.get()));
		}

		public CategoryBuilder<T> addTypedRecipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
			Supplier<RecipeType<? extends T>> excluded) {
			return addRecipeListConsumer(recipes -> {
				List<Recipe<?>> excludedRecipes = getTypedRecipes(excluded.get());
				consumeTypedRecipes(recipe -> {
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
			pred = c -> configValue.apply(c)
				.get();
			return this;
		}

		public CategoryBuilder<T> enableWhenBool(Function<CRecipes, Boolean> configValue) {
			pred = configValue::apply;
			return this;
		}

		public CreateRecipeCategory<T> build() {
				category.recipes.add(() -> {
					List<Recipe<?>> recipes = new ArrayList<>();
					if (pred.test(AllConfigs.SERVER.recipes)) {
						for (Consumer<List<Recipe<?>>> consumer : recipeListConsumers)
							consumer.accept(recipes);
					}
					return recipes;
				});
			allCategories.add(category);
			return category;
		}

	}

	public static void consumeAllRecipes(Consumer<Recipe<?>> consumer) {
		Minecraft.getInstance().level.getRecipeManager()
			.getRecipes()
			.forEach(consumer);
	}

	public static void consumeTypedRecipes(Consumer<Recipe<?>> consumer, RecipeType<?> type) {
		Map<ResourceLocation, Recipe<?>> map = Minecraft.getInstance()
			.getConnection()
			.getRecipeManager().recipes.get(type);
		if (map != null) {
			map.values()
				.forEach(consumer);
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
		if (recipe2.getIngredients()
			.get(0)
			.test(matchingStacks[0]))
			return true;
		return false;
	}

}
