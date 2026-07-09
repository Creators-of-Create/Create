package com.simibubi.create.content.kinetics.crafter;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.foundation.mixin.accessor.ShapedRecipeAccessor;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

public class MechanicalCraftingRecipe extends NormalCraftingRecipe {
	private final ShapedRecipePattern pattern;
	private final ItemStackTemplate result;
	private final boolean acceptMirrored;

	public MechanicalCraftingRecipe(String groupIn, CraftingBookCategory category,
									ShapedRecipePattern pattern, ItemStack recipeOutputIn, boolean acceptMirrored) {
		this(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(category, groupIn), pattern,
			ItemStackTemplate.fromNonEmptyStack(recipeOutputIn), acceptMirrored);
	}

	public MechanicalCraftingRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
									ShapedRecipePattern pattern, ItemStackTemplate recipeOutputIn, boolean acceptMirrored) {
		super(commonInfo, bookInfo);
		this.pattern = pattern;
		this.result = recipeOutputIn;
		this.acceptMirrored = acceptMirrored;
	}

	private static MechanicalCraftingRecipe fromShaped(ShapedRecipe recipe, boolean acceptMirrored) {
		return new MechanicalCraftingRecipe(new Recipe.CommonInfo(recipe.showNotification()),
			new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
			((ShapedRecipeAccessor) recipe).create$getPattern(),
			((ShapedRecipeAccessor) recipe).create$getResult(), acceptMirrored);
	}

	private ShapedRecipe asShapedRecipe() {
		return new ShapedRecipe(commonInfo, bookInfo, pattern, result);
	}

	@Override
	public boolean matches(CraftingInput input, Level worldIn) {
		if (!(input instanceof MechanicalCraftingInput))
			return false;
		if (acceptsMirrored())
			return pattern.matches(input);

		// From ShapedRecipe except the symmetry
		for (int i = 0; i <= input.width() - this.getWidth(); ++i)
			for (int j = 0; j <= input.height() - this.getHeight(); ++j)
				if (this.matchesSpecific(input, i, j))
					return true;
		return false;
	}

	// From ShapedRecipe
	private boolean matchesSpecific(CraftingInput input, int p_77573_2_, int p_77573_3_) {
		List<Optional<Ingredient>> ingredients = getIngredients();
		int width = getWidth();
		int height = getHeight();
		for (int i = 0; i < input.width(); ++i) {
			for (int j = 0; j < input.height(); ++j) {
				int k = i - p_77573_2_;
				int l = j - p_77573_3_;
				Optional<Ingredient> ingredient = Optional.empty();
				if (k >= 0 && l >= 0 && k < width && l < height)
					ingredient = ingredients.get(k + l * width);
				ItemStack stack = input.getItem(i + j * input.width());
				if (ingredient.isPresent() ? !ingredient.get().test(stack) : !stack.isEmpty())
					return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return result.create();
	}

	public List<Optional<Ingredient>> getIngredients() {
		return pattern.ingredients();
	}

	@Override
	protected PlacementInfo createPlacementInfo() {
		return PlacementInfo.createFromOptionals(pattern.ingredients());
	}

	public int getWidth() {
		return pattern.width();
	}

	public int getHeight() {
		return pattern.height();
	}

	@Override
	@SuppressWarnings("unchecked")
	public RecipeType<CraftingRecipe> getType() {
		return (RecipeType<CraftingRecipe>) (RecipeType<?>) AllRecipeTypes.MECHANICAL_CRAFTING.getType();
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public @NotNull RecipeSerializer<? extends NormalCraftingRecipe> getSerializer() {
		return AllRecipeTypes.MECHANICAL_CRAFTING.getSerializer();
	}

	public boolean acceptsMirrored() {
		return acceptMirrored;
	}

	public static class Serializer {
		public static final MapCodec<MechanicalCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ShapedRecipe.SERIALIZER.codec().forGetter(MechanicalCraftingRecipe::asShapedRecipe),
			Codec.BOOL.fieldOf("accept_mirrored").forGetter(MechanicalCraftingRecipe::acceptsMirrored)
		).apply(instance, MechanicalCraftingRecipe::fromShaped));

		public static final StreamCodec<RegistryFriendlyByteBuf, MechanicalCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
			ShapedRecipe.STREAM_CODEC, MechanicalCraftingRecipe::asShapedRecipe,
			ByteBufCodecs.BOOL, i -> i.acceptMirrored,
			MechanicalCraftingRecipe::fromShaped
		);

		private static final RecipeSerializer<MechanicalCraftingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

		public static RecipeSerializer<MechanicalCraftingRecipe> create() {
			return SERIALIZER;
		}
	}
}
