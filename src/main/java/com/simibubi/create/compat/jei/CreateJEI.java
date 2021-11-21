package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
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
import com.simibubi.create.compat.jei.category.FanSmokingCategory;
import com.simibubi.create.compat.jei.category.FanWashingCategory;
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
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.curiosities.toolbox.ToolboxScreen;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateScreen;
import com.simibubi.create.content.logistics.item.LinkedControllerScreen;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.item.filter.AttributeFilterScreen;
import com.simibubi.create.content.logistics.item.filter.FilterScreen;
import com.simibubi.create.content.schematics.block.SchematicTableScreen;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;

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

		smoking = register("fan_smoking", FanSmokingCategory::new).recipes(() -> IRecipeType.SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
			.build(),

		blasting = register("fan_blasting", FanBlastingCategory::new)
			.recipesExcluding(() -> IRecipeType.SMELTING, () -> IRecipeType.BLASTING)
			.recipes(() -> IRecipeType.BLASTING)
			.removeRecipes(() -> IRecipeType.SMOKING)
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
			.recipes(r -> r.getSerializer() == IRecipeSerializer.SHAPELESS_RECIPE && r.getIngredients()
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
				recipe -> recipe.getType() == IRecipeType.STONECUTTING && !AllRecipeTypes.isManualRecipe(recipe))))
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
			.recipes(r -> (r instanceof ICraftingRecipe) && MechanicalPressTileEntity.canCompress(r)
				&& !AllRecipeTypes.isManualRecipe(r), BasinRecipe::convertShapeless)
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
			.recipes(r -> r.getSerializer() == IRecipeSerializer.SHAPELESS_RECIPE && r.getIngredients()
				.size() == 1)
			.recipes(r -> (r.getType() == IRecipeType.CRAFTING
				&& r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.getType()) && (r instanceof ShapedRecipe)
				&& !AllRecipeTypes.isManualRecipe(r))
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
			.enableWhen(c -> c.allowRegularCraftingInCrafter)
			.build(),

		mechanicalCrafting =
			register("mechanical_crafting", MechanicalCraftingCategory::new).recipes(AllRecipeTypes.MECHANICAL_CRAFTING)
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.build();

	private <T extends IRecipe<?>> CategoryBuilder<T> register(String name,
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		SlotMover slotMover = new SlotMover();
		registration.addGuiContainerHandler(SchematicTableScreen.class, slotMover);
		registration.addGuiContainerHandler(SchematicannonScreen.class, slotMover);
		registration.addGuiContainerHandler(AdjustableCrateScreen.class, slotMover);
		registration.addGuiContainerHandler(FilterScreen.class, slotMover);
		registration.addGuiContainerHandler(AttributeFilterScreen.class, slotMover);
		registration.addGuiContainerHandler(BlueprintScreen.class, slotMover);
		registration.addGuiContainerHandler(LinkedControllerScreen.class, slotMover);
		registration.addGuiContainerHandler(ToolboxScreen.class, slotMover);

		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
	}

	private class CategoryBuilder<T extends IRecipe<?>> {
		private CreateRecipeCategory<T> category;
		private List<Consumer<List<IRecipe<?>>>> recipeListConsumers = new ArrayList<>();
		private Predicate<CRecipes> pred;

		public CategoryBuilder(String name, Supplier<CreateRecipeCategory<T>> category) {
			this.category = category.get();
			this.category.setCategoryId(name);
			pred = Predicates.alwaysTrue();
		}

		public CategoryBuilder<T> recipes(IRecipeTypeInfo recipeTypeEntry) {
			return recipes(recipeTypeEntry::getType);
		}

		public CategoryBuilder<T> recipes(Supplier<IRecipeType<? extends T>> recipeType) {
			return recipes(r -> r.getType() == recipeType.get());
		}

		public CategoryBuilder<T> recipes(ResourceLocation serializer) {
			return recipes(r -> r.getSerializer()
				.getRegistryName()
				.equals(serializer));
		}

		public CategoryBuilder<T> recipes(Predicate<IRecipe<?>> pred) {
			return recipeList(() -> findRecipes(pred));
		}

		public CategoryBuilder<T> recipes(Predicate<IRecipe<?>> pred, Function<IRecipe<?>, T> converter) {
			return recipeList(() -> findRecipes(pred), converter);
		}

		public CategoryBuilder<T> recipeList(Supplier<List<? extends IRecipe<?>>> list) {
			return recipeList(list, null);
		}

		public CategoryBuilder<T> recipeList(Supplier<List<? extends IRecipe<?>>> list,
			Function<IRecipe<?>, T> converter) {
			recipeListConsumers.add(recipes -> {
				List<? extends IRecipe<?>> toAdd = list.get();
				if (converter != null)
					toAdd = toAdd.stream()
						.map(converter)
						.collect(Collectors.toList());
				recipes.addAll(toAdd);
			});
			return this;
		}

		public CategoryBuilder<T> recipesExcluding(Supplier<IRecipeType<? extends T>> recipeType,
			Supplier<IRecipeType<? extends T>> excluded) {
			recipeListConsumers.add(recipes -> {
				recipes.addAll(findRecipesByTypeExcluding(recipeType.get(), excluded.get()));
			});
			return this;
		}

		public CategoryBuilder<T> removeRecipes(Supplier<IRecipeType<? extends T>> recipeType) {
			recipeListConsumers.add(recipes -> {
				removeRecipesByType(recipes, recipeType.get());
			});
			return this;
		}

		public CategoryBuilder<T> catalyst(Supplier<IItemProvider> supplier) {
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
					List<IRecipe<?>> recipes = new ArrayList<>();
					for (Consumer<List<IRecipe<?>>> consumer : recipeListConsumers)
						consumer.accept(recipes);
					return recipes;
				});
			allCategories.add(category);
			return category;
		}

	}

	public static List<IRecipe<?>> findRecipes(Predicate<IRecipe<?>> predicate) {
		return Minecraft.getInstance().level.getRecipeManager()
			.getRecipes()
			.stream()
			.filter(predicate)
			.collect(Collectors.toList());
	}

	public static List<IRecipe<?>> findRecipesByType(IRecipeType<?> type) {
		return findRecipes(recipe -> recipe.getType() == type);
	}

	public static List<IRecipe<?>> findRecipesByTypeExcluding(IRecipeType<?> type, IRecipeType<?> excludingType) {
		List<IRecipe<?>> byType = findRecipesByType(type);
		removeRecipesByType(byType, excludingType);
		return byType;
	}

	public static List<IRecipe<?>> findRecipesByTypeExcluding(IRecipeType<?> type, IRecipeType<?>... excludingTypes) {
		List<IRecipe<?>> byType = findRecipesByType(type);
		for (IRecipeType<?> excludingType : excludingTypes)
			removeRecipesByType(byType, excludingType);
		return byType;
	}

	public static void removeRecipesByType(List<IRecipe<?>> recipes, IRecipeType<?> type) {
		List<IRecipe<?>> byType = findRecipesByType(type);
		recipes.removeIf(recipe -> {
			for (IRecipe<?> r : byType)
				if (doInputsMatch(recipe, r))
					return true;
			return false;
		});
	}

	public static boolean doInputsMatch(IRecipe<?> recipe1, IRecipe<?> recipe2) {
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
