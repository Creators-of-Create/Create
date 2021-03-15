package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.simibubi.create.compat.jei.category.BlockCuttingCategory;
import com.simibubi.create.compat.jei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.jei.category.BlockzapperUpgradeCategory;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.CrushingCategory;
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
import com.simibubi.create.compat.jei.category.SpoutCategory;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.logistics.block.inventories.AdjustableCrateScreen;
import com.simibubi.create.content.logistics.item.filter.AbstractFilterScreen;
import com.simibubi.create.content.schematics.block.SchematicTableScreen;
import com.simibubi.create.content.schematics.block.SchematicannonScreen;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
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

	private static final ResourceLocation ID = new ResourceLocation(Create.ID, "jei_plugin");

	@Override
	@Nonnull
	public ResourceLocation getPluginUid() {
		return ID;
	}

	public IIngredientManager ingredientManager;
	final List<CreateRecipeCategory<?>> ALL = new ArrayList<>();
	final CreateRecipeCategory<?>

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
			.recipesExcluding(() -> IRecipeType.SMELTING, () -> IRecipeType.SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
			.build(),

		blockzapper = register("blockzapper_upgrade", BlockzapperUpgradeCategory::new)
			.recipes(AllRecipeTypes.BLOCKZAPPER_UPGRADE.serializer.getRegistryName())
			.catalyst(AllItems.BLOCKZAPPER::get)
			.build(),

		mixing = register("mixing", MixingCategory::standard).recipes(AllRecipeTypes.MIXING::getType)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

		autoShapeless = register("automatic_shapeless", MixingCategory::autoShapeless)
			.recipes(r -> r.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS && r.getIngredients()
				.size() > 1 && !MechanicalPressTileEntity.canCompress(r.getIngredients()),
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
			.recipeList(() -> CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(IRecipeType.STONECUTTING)))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhen(c -> c.allowStonecuttingOnSaw)
			.build(),

		woodCutting = register("wood_cutting", () -> new BlockCuttingCategory(Items.OAK_STAIRS))
			.recipeList(() -> CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(SawTileEntity.woodcuttingRecipeType.getValue())))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhenBool(c -> c.allowWoodcuttingOnSaw.get() && ModList.get().isLoaded("druidcraft"))
			.build(),

		packing = register("packing", PackingCategory::standard).recipes(AllRecipeTypes.COMPACTING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.build(),

		autoSquare = register("automatic_packing", PackingCategory::autoSquare)
			.recipes(r -> (r instanceof ICraftingRecipe) && MechanicalPressTileEntity.canCompress(r.getIngredients()),
				BasinRecipe::convertShapeless)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> c.allowShapedSquareInPress)
			.build(),

		polishing = register("sandpaper_polishing", PolishingCategory::new).recipes(AllRecipeTypes.SANDPAPER_POLISHING)
			.catalyst(AllItems.SAND_PAPER::get)
			.catalyst(AllItems.RED_SAND_PAPER::get)
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
			.recipes(r -> r.getSerializer() == IRecipeSerializer.CRAFTING_SHAPELESS && r.getIngredients()
				.size() == 1)
			.recipes(
				r -> (r.getType() == IRecipeType.CRAFTING && r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.type)
					&& (r instanceof ShapedRecipe))
			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
			.enableWhen(c -> c.allowRegularCraftingInCrafter)
			.build(),

		mechanicalCrafting =
			register("mechanical_crafting", MechanicalCraftingCategory::new).recipes(AllRecipeTypes.MECHANICAL_CRAFTING)
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.build()

	;

	private <T extends IRecipe<?>> CategoryBuilder<T> register(String name,
		Supplier<CreateRecipeCategory<T>> supplier) {
		return new CategoryBuilder<T>(name, supplier);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(AllItems.BLOCKZAPPER.get());
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		ALL.forEach(registration::addRecipeCategories);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		ingredientManager = registration.getIngredientManager();
		ALL.forEach(c -> c.recipes.forEach(s -> registration.addRecipes(s.get(), c.getUid())));
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		ALL.forEach(c -> c.recipeCatalysts.forEach(s -> registration.addRecipeCatalyst(s.get(), c.getUid())));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		SlotMover slotMover = new SlotMover();
		registration.addGuiContainerHandler(AdjustableCrateScreen.class, slotMover);
		registration.addGuiContainerHandler(SchematicannonScreen.class, slotMover);
		registration.addGuiContainerHandler(SchematicTableScreen.class, slotMover);
		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new FilterGhostIngredientHandler());
	}

	private class CategoryBuilder<T extends IRecipe<?>> {
		CreateRecipeCategory<T> category;
		private Predicate<CRecipes> pred;

		CategoryBuilder(String name, Supplier<CreateRecipeCategory<T>> category) {
			this.category = category.get();
			this.category.setCategoryId(name);
			this.pred = Predicates.alwaysTrue();
		}

		CategoryBuilder<T> catalyst(Supplier<IItemProvider> supplier) {
			return catalystStack(() -> new ItemStack(supplier.get()
				.asItem()));
		}

		CategoryBuilder<T> catalystStack(Supplier<ItemStack> supplier) {
			category.recipeCatalysts.add(supplier);
			return this;
		}

		CategoryBuilder<T> recipes(AllRecipeTypes recipeTypeEntry) {
			return recipes(recipeTypeEntry::getType);
		}

		CategoryBuilder<T> recipes(Supplier<IRecipeType<T>> recipeType) {
			return recipes(r -> r.getType() == recipeType.get());
		}

		CategoryBuilder<T> recipes(ResourceLocation serializer) {
			return recipes(r -> r.getSerializer()
				.getRegistryName()
				.equals(serializer));
		}

		CategoryBuilder<T> recipes(Predicate<IRecipe<?>> pred) {
			return recipeList(() -> findRecipes(pred));
		}

		CategoryBuilder<T> recipes(Predicate<IRecipe<?>> pred, Function<IRecipe<?>, T> converter) {
			return recipeList(() -> findRecipes(pred), converter);
		}

		CategoryBuilder<T> recipeList(Supplier<List<? extends IRecipe<?>>> list) {
			return recipeList(list, null);
		}

		CategoryBuilder<T> recipeList(Supplier<List<? extends IRecipe<?>>> list, Function<IRecipe<?>, T> converter) {
			category.recipes.add(() -> {
				if (!this.pred.test(AllConfigs.SERVER.recipes))
					return Collections.emptyList();
				if (converter != null)
					return list.get()
						.stream()
						.map(converter)
						.collect(Collectors.toList());
				return list.get();
			});
			return this;
		}

		CategoryBuilder<T> recipesExcluding(Supplier<IRecipeType<? extends T>> recipeType,
			Supplier<IRecipeType<? extends T>> excluded) {
			category.recipes.add(() -> {
				if (!this.pred.test(AllConfigs.SERVER.recipes))
					return Collections.emptyList();
				return findRecipesByTypeExcluding(recipeType.get(), excluded.get());
			});
			return this;
		}

		CategoryBuilder<T> enableWhen(Function<CRecipes, ConfigBool> configValue) {
			this.pred = c -> configValue.apply(c)
				.get();
			return this;
		}

		CategoryBuilder<T> enableWhenBool(Function<CRecipes, Boolean> configValue) {
			this.pred = configValue::apply;
			return this;
		}

		CreateRecipeCategory<T> build() {
			ALL.add(category);
			return category;
		}

	}

	static List<IRecipe<?>> findRecipesByType(IRecipeType<?> type) {
		return findRecipes(r -> r.getType() == type);
	}

	static List<IRecipe<?>> findRecipes(Predicate<IRecipe<?>> predicate) {
		return Minecraft.getInstance().world.getRecipeManager()
			.getRecipes()
			.stream()
			.filter(predicate)
			.collect(Collectors.toList());
	}

	static List<IRecipe<?>> findRecipesByTypeExcluding(IRecipeType<?> type, IRecipeType<?> excludingType) {
		List<IRecipe<?>> byType = findRecipes(r -> r.getType() == type);
		List<IRecipe<?>> byExcludingType = findRecipes(r -> r.getType() == excludingType);
		byType.removeIf(recipe -> {
			for (IRecipe<?> r : byExcludingType) {
				ItemStack[] matchingStacks = recipe.getIngredients()
					.get(0)
					.getMatchingStacks();
				if (matchingStacks.length == 0)
					return true;
				if (r.getIngredients()
					.get(0)
					.test(matchingStacks[0]))
					return true;
			}
			return false;
		});
		return byType;
	}

}
