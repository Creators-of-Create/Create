package com.simibubi.create.content.contraptions.itemAssembly;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class SequencedAssemblyRecipe implements IRecipe<RecipeWrapper> {

	ResourceLocation id;
	SequencedAssemblyRecipeSerializer serializer;

	Ingredient ingredient;
	List<SequencedRecipe<?>> sequence;
	int averageSteps;
	int maxSteps;
	ProcessingOutput transitionalItem;
	List<ProcessingOutput> resultPool;

	public SequencedAssemblyRecipe(ResourceLocation recipeId, SequencedAssemblyRecipeSerializer serializer) {
		this.id = recipeId;
		this.serializer = serializer;
		sequence = new ArrayList<>();
		resultPool = new ArrayList<>();
		averageSteps = 16;
		maxSteps = 32;
	}

	public static <C extends IInventory, R extends ProcessingRecipe<C>> Optional<R> getRecipe(World world, C inv,
		IRecipeType<R> type, Class<R> recipeClass) {
		return getRecipe(world, inv.getStackInSlot(0), type, recipeClass).filter(r -> r.matches(inv, world));
	}

	public static <R extends ProcessingRecipe<?>> Optional<R> getRecipe(World world, ItemStack item,
		IRecipeType<R> type, Class<R> recipeClass) {
		List<SequencedAssemblyRecipe> all = world.getRecipeManager()
			.<RecipeWrapper, SequencedAssemblyRecipe>listAllOfType(AllRecipeTypes.SEQUENCED_ASSEMBLY.getType());
		for (SequencedAssemblyRecipe sequencedAssemblyRecipe : all) {
			if (!sequencedAssemblyRecipe.appliesTo(item))
				continue;
			SequencedRecipe<?> nextRecipe = sequencedAssemblyRecipe.getNextRecipe(item);
			if (nextRecipe.wrapped.getType() != type || !recipeClass.isInstance(nextRecipe.wrapped))
				continue;
			nextRecipe.wrapped.enforceNextResult(() -> sequencedAssemblyRecipe.advance(item));
			return Optional.of(recipeClass.cast(nextRecipe.wrapped));
		}
		return Optional.empty();
	}

	private ItemStack advance(ItemStack input) {
		int step = getStep(input);
		if (step >= sequence.size()) {
			float chance = 1f / (averageSteps - sequence.size());
			if (step >= maxSteps || Create.RANDOM.nextFloat() < chance)
				return rollResult();
		}

		ItemStack advancedItem = ItemHandlerHelper.copyStackWithSize(transitionalItem.getStack(), 1);
		CompoundNBT itemTag = advancedItem.getOrCreateTag();
		CompoundNBT tag = new CompoundNBT();
		tag.putString("id", id.toString());
		tag.putInt("Step", step + 1);
		itemTag.put("SequencedAssembly", tag);
		advancedItem.setTag(itemTag);
		return advancedItem;
	}

	private ItemStack rollResult() {
		float totalWeight = 0;
		for (ProcessingOutput entry : resultPool)
			totalWeight += entry.getChance();
		float number = Create.RANDOM.nextFloat() * totalWeight;
		for (ProcessingOutput entry : resultPool) {
			number -= entry.getChance();
			if (number < 0)
				return entry.getStack()
					.copy();
		}
		return ItemStack.EMPTY;
	}

	private boolean appliesTo(ItemStack input) {
		if (ingredient.test(input))
			return true;
		return input.hasTag() && transitionalItem.getStack()
			.getItem() == input.getItem() && input.getTag()
				.contains("SequencedAssembly")
			&& input.getTag()
				.getCompound("SequencedAssembly")
				.getString("id")
				.equals(id.toString());
	}

	private SequencedRecipe<?> getNextRecipe(ItemStack input) {
		return sequence.get(getStep(input) % sequence.size());
	}

	private int getStep(ItemStack input) {
		if (!input.hasTag())
			return 0;
		CompoundNBT tag = input.getTag();
		if (!tag.contains("SequencedAssembly"))
			return 0;
		int step = tag.getCompound("SequencedAssembly")
			.getInt("Step");
		return step;
	}

	@Override
	public boolean matches(RecipeWrapper inv, World p_77569_2_) {
		return false;
	}

	@Override
	public ItemStack getCraftingResult(RecipeWrapper p_77572_1_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int p_194133_1_, int p_194133_2_) {
		return false;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return resultPool.get(0)
			.getStack();
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return serializer;
	}

	@Override
	public IRecipeType<?> getType() {
		return AllRecipeTypes.SEQUENCED_ASSEMBLY.getType();
	}

	@OnlyIn(Dist.CLIENT)
	public static void addToTooltip(List<ITextComponent> toolTip, ItemStack stack) {
		if (!stack.hasTag() || !stack.getTag()
			.contains("SequencedAssembly"))
			return;
		CompoundNBT compound = stack.getTag()
			.getCompound("SequencedAssembly");
		ResourceLocation resourceLocation = new ResourceLocation(compound.getString("id"));
		Optional<? extends IRecipe<?>> recipe = Minecraft.getInstance().world.getRecipeManager()
			.getRecipe(resourceLocation);
		if (!recipe.isPresent())
			return;
		IRecipe<?> iRecipe = recipe.get();
		if (!(iRecipe instanceof SequencedAssemblyRecipe))
			return;

		SequencedAssemblyRecipe sequencedAssemblyRecipe = (SequencedAssemblyRecipe) iRecipe;
		toolTip.add(new StringTextComponent(""));
		toolTip.add(Lang.translate("recipe.sequenced_assembly")
			.formatted(TextFormatting.GRAY));
		int step = sequencedAssemblyRecipe.getStep(stack);

		for (int i = 0; i < sequencedAssemblyRecipe.sequence.size(); i++) {
			SequencedRecipe<?> sequencedRecipe =
				sequencedAssemblyRecipe.sequence.get((i + step) % sequencedAssemblyRecipe.sequence.size());
			ITextComponent textComponent = sequencedRecipe.wrapped.getDescriptionForAssembly();
			if (i == 0)
				toolTip.add(Lang.translate("recipe.assembly.next", textComponent)
					.formatted(TextFormatting.AQUA));
			else
				toolTip.add(new StringTextComponent("-> ").append(textComponent)
					.formatted(TextFormatting.DARK_AQUA));
		}

	}

}
