package com.simibubi.create.content.contraptions.itemAssembly;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
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

	protected ResourceLocation id;
	protected SequencedAssemblyRecipeSerializer serializer;

	protected Ingredient ingredient;
	protected List<SequencedRecipe<?>> sequence;
	protected int loops;
	protected ProcessingOutput transitionalItem;
	protected List<ProcessingOutput> resultPool;

	public SequencedAssemblyRecipe(ResourceLocation recipeId, SequencedAssemblyRecipeSerializer serializer) {
		this.id = recipeId;
		this.serializer = serializer;
		sequence = new ArrayList<>();
		resultPool = new ArrayList<>();
		loops = 5;
	}

	public static <C extends IInventory, R extends ProcessingRecipe<C>> Optional<R> getRecipe(World world, C inv,
		IRecipeType<R> type, Class<R> recipeClass) {
		//return getRecipe(world, inv.getStackInSlot(0), type, recipeClass).filter(r -> r.matches(inv, world));
		return getRecipes(world, inv.getItem(0), type, recipeClass).filter(r -> r.matches(inv, world))
				.findFirst();
	}

	public static <R extends ProcessingRecipe<?>> Optional<R> getRecipe(World world, ItemStack item,
		IRecipeType<R> type, Class<R> recipeClass) {
		List<SequencedAssemblyRecipe> all = world.getRecipeManager()
			.<RecipeWrapper, SequencedAssemblyRecipe>getAllRecipesFor(AllRecipeTypes.SEQUENCED_ASSEMBLY.getType());
		for (SequencedAssemblyRecipe sequencedAssemblyRecipe : all) {
			if (!sequencedAssemblyRecipe.appliesTo(item))
				continue;
			SequencedRecipe<?> nextRecipe = sequencedAssemblyRecipe.getNextRecipe(item);
			ProcessingRecipe<?> recipe = nextRecipe.getRecipe();
			if (recipe.getType() != type || !recipeClass.isInstance(recipe))
				continue;
			recipe.enforceNextResult(() -> sequencedAssemblyRecipe.advance(item));
			return Optional.of(recipeClass.cast(recipe));
		}
		return Optional.empty();
	}

	public static <R extends ProcessingRecipe<?>> Stream<R> getRecipes(World world, ItemStack item,
		IRecipeType<R> type, Class<R> recipeClass) {
		List<SequencedAssemblyRecipe> all = world.getRecipeManager()
			.<RecipeWrapper, SequencedAssemblyRecipe>getAllRecipesFor(AllRecipeTypes.SEQUENCED_ASSEMBLY.getType());

		return all.stream()
				.filter(it -> it.appliesTo(item))
				.map(it -> Pair.of(it, it.getNextRecipe(item).getRecipe()))
				.filter(it -> it.getSecond()
						.getType() == type && recipeClass.isInstance(it.getSecond()))
				.map(it -> {
					it.getSecond()
							.enforceNextResult(() -> it.getFirst().advance(item));
					return it.getSecond();
				})
				.map(recipeClass::cast);
	}

	private ItemStack advance(ItemStack input) {
		int step = getStep(input);
		if ((step + 1) / sequence.size() >= loops)
			return rollResult();

		ItemStack advancedItem = ItemHandlerHelper.copyStackWithSize(getTransitionalItem(), 1);
		CompoundNBT itemTag = advancedItem.getOrCreateTag();
		CompoundNBT tag = new CompoundNBT();
		tag.putString("id", id.toString());
		tag.putInt("Step", step + 1);
		tag.putFloat("Progress", (step + 1f) / (sequence.size() * loops));
		itemTag.put("SequencedAssembly", tag);
		advancedItem.setTag(itemTag);
		return advancedItem;
	}

	public int getLoops() {
		return loops;
	}

	public void addAdditionalIngredientsAndMachines(List<Ingredient> list) {
		sequence.forEach(sr -> sr.getAsAssemblyRecipe()
			.addAssemblyIngredients(list));
		Set<IItemProvider> machines = new HashSet<>();
		sequence.forEach(sr -> sr.getAsAssemblyRecipe()
			.addRequiredMachines(machines));
		machines.stream()
			.map(Ingredient::of)
			.forEach(list::add);
	}

	public void addAdditionalFluidIngredients(List<FluidIngredient> list) {
		sequence.forEach(sr -> sr.getAsAssemblyRecipe()
			.addAssemblyFluidIngredients(list));
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
		return input.hasTag() && getTransitionalItem().getItem() == input.getItem() && input.getTag()
			.contains("SequencedAssembly") && input.getTag()
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
	public ItemStack assemble(RecipeWrapper p_77572_1_) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int p_194133_1_, int p_194133_2_) {
		return false;
	}

	@Override
	public ItemStack getResultItem() {
		return resultPool.get(0)
			.getStack();
	}

	public float getOutputChance() {
		float totalWeight = 0;
		for (ProcessingOutput entry : resultPool)
			totalWeight += entry.getChance();
		return resultPool.get(0)
			.getChance() / totalWeight;
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
	public boolean isSpecial() {
		return true;
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
		Optional<? extends IRecipe<?>> recipe = Minecraft.getInstance().level.getRecipeManager()
			.byKey(resourceLocation);
		if (!recipe.isPresent())
			return;
		IRecipe<?> iRecipe = recipe.get();
		if (!(iRecipe instanceof SequencedAssemblyRecipe))
			return;

		SequencedAssemblyRecipe sequencedAssemblyRecipe = (SequencedAssemblyRecipe) iRecipe;
		int length = sequencedAssemblyRecipe.sequence.size();
		int step = sequencedAssemblyRecipe.getStep(stack);
		int total = length * sequencedAssemblyRecipe.loops;
		toolTip.add(new StringTextComponent(""));
		toolTip.add(Lang.translate("recipe.sequenced_assembly")
			.withStyle(TextFormatting.GRAY));
		toolTip.add(Lang.translate("recipe.assembly.progress", step, total)
			.withStyle(TextFormatting.DARK_GRAY));

		int remaining = total - step;
		for (int i = 0; i < length; i++) {
			if (i >= remaining)
				break;
			SequencedRecipe<?> sequencedRecipe = sequencedAssemblyRecipe.sequence.get((i + step) % length);
			ITextComponent textComponent = sequencedRecipe.getAsAssemblyRecipe()
				.getDescriptionForAssembly();
			if (i == 0)
				toolTip.add(Lang.translate("recipe.assembly.next", textComponent)
					.withStyle(TextFormatting.AQUA));
			else
				toolTip.add(new StringTextComponent("-> ").append(textComponent)
					.withStyle(TextFormatting.DARK_AQUA));
		}

	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public List<SequencedRecipe<?>> getSequence() {
		return sequence;
	}

	public ItemStack getTransitionalItem() {
		return transitionalItem.getStack();
	}

}
