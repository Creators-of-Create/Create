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
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

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

	soul_smoking = register("fan_haunting", FanHauntingCategory::new).recipes(AllRecipeTypes.HAUNTING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_haunting"))
			.build(),

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
				.size() > 1 && !MechanicalPressTileEntity.canCompress((Recipe<?>) r),
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
			.recipeList(() -> CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(RecipeType.STONECUTTING)))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhen(c -> c.allowStonecuttingOnSaw)
			.build(),

	woodCutting = register("wood_cutting", () -> new BlockCuttingCategory(Items.OAK_STAIRS))
			.recipeList(() -> CondensedBlockCuttingRecipe
				.condenseRecipes(findRecipesByType(SawTileEntity.woodcuttingRecipeType.get())))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhenBool(c -> c.allowWoodcuttingOnSaw.get() && FabricLoader.getInstance()
				.isModLoaded("druidcraft"))
			.build(),

	packing = register("packing", PackingCategory::standard).recipes(AllRecipeTypes.COMPACTING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

	autoSquare = register("automatic_packing", PackingCategory::autoSquare)
			.recipes(re -> (re instanceof CraftingRecipe r) && MechanicalPressTileEntity.canCompress(r),
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
			.recipeList(
				() -> DeployerApplicationRecipe.convert(findRecipesByType(AllRecipeTypes.SANDPAPER_POLISHING.getType())))
			.recipes(AllRecipeTypes.DEPLOYING)
			.catalyst(AllBlocks.DEPLOYER::get)
			.catalyst(AllBlocks.DEPOT::get)
			.catalyst(AllItems.BELT_CONNECTOR::get)
			.build(),

	mysteryConversion = register("mystery_conversion", MysteriousItemConversionCategory::new)
			.recipeList(MysteriousItemConversionCategory::getRecipes)
			.build(),

	spoutFilling = register("spout_filling", SpoutCategory::new).recipes(AllRecipeTypes.FILLING)
			.recipeList(SpoutCategory::getRecipes)
			.catalyst(AllBlocks.SPOUT::get)
			.build(),

	draining = register("draining", ItemDrainCategory::new)
			.recipeList(ItemDrainCategory::getRecipes)
			.recipes(AllRecipeTypes.EMPTYING)
			.catalyst(AllBlocks.ITEM_DRAIN::get)
			.build(),

	autoShaped = register("automatic_shaped", MechanicalCraftingCategory::new)
			.recipes(r -> r.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE && r.getIngredients()
				.size() == 1)
			.recipes(
				r -> (r.getType() == RecipeType.CRAFTING && r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.getType())
					&& (r instanceof ShapedRecipe))
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
			.enableWhen(c -> c.allowRegularCraftingInCrafter)
			.build(),

	mechanicalCrafting = register("mechanical_crafting", MechanicalCraftingCategory::new)
			.recipes(AllRecipeTypes.MECHANICAL_CRAFTING)
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
			.build();

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
		allCategories.forEach(c -> c.recipeCatalysts.forEach(s -> registry.addWorkstations(c.getCategoryIdentifier(), EntryStack.of(VanillaEntryTypes.ITEM, ((Supplier<ItemStack> )s).get()))));
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		allCategories.forEach(c -> c.recipes.forEach(s -> {
			for (Recipe<?> recipe : s.get()) {
				registry.add(new CreateDisplay<>(recipe, c.getCategoryIdentifier().getPath()), recipe);
			}
		}));

		List<CraftingRecipe> recipes = ToolboxColoringRecipeMaker.createRecipes().toList();
		for (Object recipe : recipes) {
			Collection<Display> displays = registry.tryFillDisplay(recipe);
			for (Display display : displays) {
				if (Objects.equals(display.getCategoryIdentifier(), BuiltinPlugin.CRAFTING)) {
					registry.add(display, recipe);
				}
			}
		}
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

	@Override
	public void registerEntries(EntryRegistry registry) {
		registry.removeEntryIf(entryStack -> {
			if(entryStack.getType() == VanillaEntryTypes.ITEM) {
				ItemStack itemStack = entryStack.castValue();
				if(itemStack.getItem() instanceof TagDependentIngredientItem tagItem) {
					return tagItem.shouldHide();
				}
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

		public CategoryBuilder<T> recipes(IRecipeTypeInfo recipeTypeEntry) {
			return recipes(recipeTypeEntry::getType);
		}

		public CategoryBuilder<T> recipes(Supplier<RecipeType<? extends T>> recipeType) {
			return recipes(r -> r.getType() == recipeType.get());
		}

		public CategoryBuilder<T> recipes(ResourceLocation serializer) {
			return recipes(r -> Registry.RECIPE_SERIALIZER.getKey(r.getSerializer())
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

	public static List<Recipe<?>> findRecipes(Predicate<Recipe<?>> predicate) {
		return Minecraft.getInstance().level.getRecipeManager()
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
