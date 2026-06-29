package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory.Factory;
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
import com.simibubi.create.content.equipment.blueprint.BlueprintScreen;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionMixingRecipes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelSetItemScreen;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerScreen;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.data.recipe.LogStrippingFakeRecipes;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.RecipeGenericsUtil;
import com.simibubi.create.infrastructure.config.AllConfigs;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.fluids.FluidStack;

@JeiPlugin
@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class CreateJEI implements IModPlugin {

	private static final ResourceLocation ID = Create.asResource("jei_plugin");

	private final List<CreateRecipeCategory<?>> allCategories = new ArrayList<>();
	private IIngredientManager ingredientManager;

	public static IJeiRuntime runtime;

	private void loadCategories() {
		allCategories.clear();

		CreateRecipeCategory<?>

			milling = builder(AbstractCrushingRecipe.class)
			.addTypedRecipes(AllRecipeTypes.MILLING)
			.catalyst(AllBlocks.MILLSTONE::get)
			.doubleItemIcon(AllBlocks.MILLSTONE.get(), AllItems.WHEAT_FLOUR.get())
			.emptyBackground(177, 53)
			.build("milling", MillingCategory::new),

			crushing = builder(AbstractCrushingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.CRUSHING)
				.addTypedRecipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
				.catalyst(AllBlocks.CRUSHING_WHEEL::get)
				.doubleItemIcon(AllBlocks.CRUSHING_WHEEL.get(), AllItems.CRUSHED_GOLD.get())
				.emptyBackground(177, 100)
				.build("crushing", CrushingCategory::new),

			pressing = builder(PressingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.PRESSING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllItems.IRON_SHEET.get())
				.emptyBackground(177, 70)
				.build("pressing", PressingCategory::new),

			washing = builder(SplashingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SPLASHING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.WATER_BUCKET)
				.emptyBackground(178, 72)
				.build("fan_washing", FanWashingCategory::new),

			smoking = builder(SmokingRecipe.class)
				.addTypedRecipes(() -> RecipeType.SMOKING)
				.removeNonAutomation()
				.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.CAMPFIRE)
				.emptyBackground(178, 72)
				.build("fan_smoking", FanSmokingCategory::new),

			blasting = builder(AbstractCookingRecipe.class)
				.addTypedRecipesExcluding(() -> RecipeType.SMELTING, () -> RecipeType.BLASTING)
				.addTypedRecipes(() -> RecipeType.BLASTING)
				.removeRecipes(() -> RecipeType.SMOKING)
				.removeNonAutomation()
				.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.LAVA_BUCKET)
				.emptyBackground(178, 72)
				.build("fan_blasting", FanBlastingCategory::new),

			haunting = builder(HauntingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.HAUNTING)
				.catalystStack(ProcessingViaFanCategory.getFan("fan_haunting"))
				.doubleItemIcon(AllItems.PROPELLER.get(), Items.SOUL_CAMPFIRE)
				.emptyBackground(178, 72)
				.build("fan_haunting", FanHauntingCategory::new),

			mixing = builder(BasinRecipe.class)
				.addTypedRecipes(AllRecipeTypes.MIXING)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), AllBlocks.BASIN.get())
				.emptyBackground(177, 103)
				.build("mixing", MixingCategory::standard),

			autoShapeless = builder(BasinRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowShapelessInMixer)
				.addAllRecipesIf(r -> r.value() instanceof CraftingRecipe && !(r.value() instanceof ShapedRecipe)
						&& r.value().getIngredients()
						.size() > 1
						&& !MechanicalPressBlockEntity.canCompress(r.value()) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
					BasinRecipe::convertShapeless)
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), Items.CRAFTING_TABLE)
				.emptyBackground(177, 85)
				.build("automatic_shapeless", MixingCategory::autoShapeless),

			brewing = builder(BasinRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowBrewingInMixer)
				.addRecipes(() -> RecipeGenericsUtil.cast(PotionMixingRecipes.createRecipes(Minecraft.getInstance().level)))
				.catalyst(AllBlocks.MECHANICAL_MIXER::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_MIXER.get(), Blocks.BREWING_STAND)
				.emptyBackground(177, 103)
				.build("automatic_brewing", MixingCategory::autoBrewing),

			packing = builder(BasinRecipe.class)
				.addTypedRecipes(AllRecipeTypes.COMPACTING)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), AllBlocks.BASIN.get())
				.emptyBackground(177, 103)
				.build("packing", PackingCategory::standard),

			autoSquare = builder(BasinRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowShapedSquareInPress)
				.addAllRecipesIf(
					r -> (r.value() instanceof CraftingRecipe) && !(r.value() instanceof MechanicalCraftingRecipe)
						&& MechanicalPressBlockEntity.canCompress(r.value()) && !AllRecipeTypes.shouldIgnoreInAutomation(r),
					BasinRecipe::convertShapeless)
				.catalyst(AllBlocks.MECHANICAL_PRESS::get)
				.catalyst(AllBlocks.BASIN::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_PRESS.get(), Blocks.CRAFTING_TABLE)
				.emptyBackground(177, 85)
				.build("automatic_packing", PackingCategory::autoSquare),

			sawing = builder(CuttingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.CUTTING)
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.OAK_LOG)
				.emptyBackground(177, 70)
				.build("sawing", SawingCategory::new),

			blockCutting = builder(CondensedBlockCuttingRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowStonecuttingOnSaw)
				.addRecipes(() -> BlockCuttingCategory.condenseRecipes(getTypedRecipesExcluding(RecipeType.STONECUTTING, AllRecipeTypes::shouldIgnoreInAutomation)))
				.catalyst(AllBlocks.MECHANICAL_SAW::get)
				.doubleItemIcon(AllBlocks.MECHANICAL_SAW.get(), Items.STONE_BRICK_STAIRS)
				.emptyBackground(177, 70)
				.build("block_cutting", BlockCuttingCategory::new),

			polishing = builder(SandPaperPolishingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING)
				.catalyst(AllItems.SAND_PAPER::get)
				.catalyst(AllItems.RED_SAND_PAPER::get)
				.itemIcon(AllItems.SAND_PAPER.get())
				.emptyBackground(177, 55)
				.build("sandpaper_polishing", PolishingCategory::new),

			item_application = builder(ItemApplicationRecipe.class)
				.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION)
				.addRecipes(() -> RecipeGenericsUtil.cast(LogStrippingFakeRecipes.createRecipes()))
				.itemIcon(AllItems.BRASS_HAND.get())
				.emptyBackground(177, 60)
				.build("item_application", ItemApplicationCategory::new),

			deploying = builder(DeployerApplicationRecipe.class)
				.addTypedRecipes(AllRecipeTypes.DEPLOYING)
				.addTypedRecipes(AllRecipeTypes.SANDPAPER_POLISHING::getType, DeployerApplicationRecipe::convert)
				.addTypedRecipes(AllRecipeTypes.ITEM_APPLICATION::getType, ManualApplicationRecipe::asDeploying)
				.removeNonAutomation()
				.catalyst(AllBlocks.DEPLOYER::get)
				.catalyst(AllBlocks.DEPOT::get)
				.catalyst(AllItems.BELT_CONNECTOR::get)
				.itemIcon(AllBlocks.DEPLOYER.get())
				.emptyBackground(177, 70)
				.build("deploying", DeployingCategory::new),

			spoutFilling = builder(FillingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.FILLING)
				.addRecipeListConsumer(recipes -> SpoutCategory.consumeRecipes(recipes::add, ingredientManager))
				.catalyst(AllBlocks.SPOUT::get)
				.doubleItemIcon(AllBlocks.SPOUT.get(), Items.WATER_BUCKET)
				.emptyBackground(177, 70)
				.build("spout_filling", SpoutCategory::new),

			draining = builder(EmptyingRecipe.class)
				.addRecipeListConsumer(recipes -> ItemDrainCategory.consumeRecipes(recipes::add, ingredientManager))
				.addTypedRecipes(AllRecipeTypes.EMPTYING)
				.catalyst(AllBlocks.ITEM_DRAIN::get)
				.doubleItemIcon(AllBlocks.ITEM_DRAIN.get(), Items.WATER_BUCKET)
				.emptyBackground(177, 50)
				.build("draining", ItemDrainCategory::new),

			autoShaped = builder(CraftingRecipe.class)
				.enableWhen(AllConfigs.server().recipes.allowRegularCraftingInCrafter)
				.addAllRecipesIf(r -> r.value() instanceof CraftingRecipe && !(r.value() instanceof ShapedRecipe)
					&& r.value().getIngredients()
					.size() == 1
					&& !AllRecipeTypes.shouldIgnoreInAutomation(r))
				.addTypedRecipesIf(() -> RecipeType.CRAFTING,
					recipe -> recipe.value() instanceof ShapedRecipe && !AllRecipeTypes.shouldIgnoreInAutomation(recipe))
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.itemIcon(AllBlocks.MECHANICAL_CRAFTER.get())
				.emptyBackground(177, 107)
				.build("automatic_shaped", MechanicalCraftingCategory::new),

			mechanicalCrafting = builder(CraftingRecipe.class)
				.addTypedRecipes(AllRecipeTypes.MECHANICAL_CRAFTING)
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.itemIcon(AllBlocks.MECHANICAL_CRAFTER.get())
				.emptyBackground(177, 107)
				.build("mechanical_crafting", MechanicalCraftingCategory::new),

			seqAssembly = builder(SequencedAssemblyRecipe.class)
				.addTypedRecipes(AllRecipeTypes.SEQUENCED_ASSEMBLY)
				.itemIcon(AllItems.PRECISION_MECHANISM.get())
				.emptyBackground(180, 115)
				.build("sequenced_assembly", SequencedAssemblyCategory::new),

			mysteryConversion = builder(ConversionRecipe.class)
				.addRecipes(() -> MysteriousItemConversionCategory.RECIPES)
				.itemIcon(AllBlocks.PECULIAR_BELL.get())
				.emptyBackground(177, 50)
				.build("mystery_conversion", MysteriousItemConversionCategory::new);

	}

	private <T extends Recipe<? extends RecipeInput>> CategoryBuilder<T> builder(Class<T> recipeClass) {
		return new CategoryBuilder<>(recipeClass);
	}

	@Override
	@NotNull
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		loadCategories();
		registration.addRecipeCategories(allCategories.toArray(IRecipeCategory[]::new));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ingredientManager = registration.getIngredientManager();

		allCategories.forEach(c -> c.registerRecipes(registration));

		registration.addRecipes(RecipeTypes.CRAFTING, ToolboxColoringRecipeMaker.createRecipes().toList());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		allCategories.forEach(c -> c.registerCatalysts(registration));
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(new BlueprintTransferHandler(), RecipeTypes.CRAFTING);
		registration.addUniversalRecipeTransferHandler(new StockKeeperTransferHandler(registration.getJeiHelpers()));
	}

	@Override
	public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
		PotionFluidSubtypeInterpreter interpreter = new PotionFluidSubtypeInterpreter();
		PotionFluid potionFluid = AllFluids.POTION.get();
		registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, potionFluid.getSource(), interpreter);
		registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, potionFluid.getFlowing(), interpreter);
	}

	@Override
	public void registerExtraIngredients(IExtraIngredientRegistration registration) {
		RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
		List<Reference<Potion>> potions = registryAccess.lookupOrThrow(Registries.POTION)
			.listElements()
			.toList();
		Collection<FluidStack> potionFluids = new ArrayList<>(potions.size() * 3);
		Set<Set<Holder<MobEffect>>> visitedEffects = new HashSet<>();
		for (Reference<Potion> potion : potions) {
			// @goshante: Ingame potion fluids always have Bottle tag that specifies
			// to what bottle type this potion belongs
			// Potion fluid without this tag wouldn't be recognized by other mods

//			for (PotionFluid.BottleType bottleType : PotionFluid.BottleType.values()) {
//				FluidStack potionFluid = PotionFluid.of(1000, new PotionContents(potion), bottleType);
//				potionFluids.add(potionFluid);
//			}

			PotionContents potionContents = new PotionContents(potion);

			if (potionContents.hasEffects()) {
				Set<Holder<MobEffect>> effectSet = new HashSet<>();
				potionContents.forEachEffect(mei -> effectSet.add(mei.getEffect()));
				if (!visitedEffects.add(effectSet))
					continue;
}

			potionFluids.add(PotionFluid.of(1000, potionContents, PotionFluid.BottleType.REGULAR));
		}
		registration.addExtraIngredients(NeoForgeTypes.FLUID_STACK, potionFluids);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());

		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(ScheduleScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(RedstoneRequesterScreen.class, new GhostIngredientHandler());
		registration.addGhostIngredientHandler(FactoryPanelSetItemScreen.class, new GhostIngredientHandler());

		registration.addGuiContainerHandler(StockKeeperRequestScreen.class, new StockKeeperGuiContainerHandler(ingredientManager));
	}

	private class CategoryBuilder<T extends Recipe<?>> extends CreateRecipeCategory.Builder<T> {
		public CategoryBuilder(Class<? extends T> recipeClass) {
			super(recipeClass);
		}

		@Override
		public CreateRecipeCategory<T> build(ResourceLocation id, Factory<T> factory) {
			CreateRecipeCategory<T> category = super.build(id, factory);
			allCategories.add(category);
			return category;
		}
	}

	public static void consumeAllRecipes(Consumer<? super RecipeHolder<?>> consumer) {
		Minecraft.getInstance()
			.getConnection()
			.getRecipeManager()
			.getRecipes()
			.forEach(consumer);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T extends Recipe<?>> void consumeTypedRecipes(Consumer<RecipeHolder<?>> consumer, RecipeType<?> type) {
		List<? extends RecipeHolder<?>> map = Minecraft.getInstance()
			.getConnection()
			.getRecipeManager().getAllRecipesFor((RecipeType) type);
		if (!map.isEmpty())
			map.forEach(consumer);
	}

	public static List<RecipeHolder<?>> getTypedRecipes(RecipeType<?> type) {
		List<RecipeHolder<?>> recipes = new ArrayList<>();
		consumeTypedRecipes(recipes::add, type);
		return recipes;
	}

	public static List<RecipeHolder<?>> getTypedRecipesExcluding(RecipeType<?> type, Predicate<RecipeHolder<?>> exclusionPred) {
		List<RecipeHolder<?>> recipes = getTypedRecipes(type);
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
			.getFirst()
			.getItems();
		if (matchingStacks.length == 0) {
			return false;
		}
		return recipe2.getIngredients()
			.getFirst()
			.test(matchingStacks[0]);
	}

	public static boolean doOutputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		RegistryAccess registryAccess = Minecraft.getInstance().level.registryAccess();
		return ItemHelper.sameItem(recipe1.getResultItem(registryAccess), recipe2.getResultItem(registryAccess));
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		CreateJEI.runtime = runtime;
	}

}
