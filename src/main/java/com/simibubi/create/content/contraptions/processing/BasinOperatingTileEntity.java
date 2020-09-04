package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.processing.BasinTileEntity.BasinInventory;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.SimpleTrigger;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class BasinOperatingTileEntity extends KineticTileEntity {

	public DeferralBehaviour basinChecker;
	public boolean basinRemoved;
	protected IRecipe<?> lastRecipe;

	protected LazyOptional<IItemHandler> basinItemInv = LazyOptional.empty();
	protected List<ItemStack> itemInputs;
	protected LazyOptional<IFluidHandler> basinFluidInv = LazyOptional.empty();
	protected List<FluidStack> fluidInputs;

	public BasinOperatingTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
		itemInputs = new ArrayList<>();
		fluidInputs = new ArrayList<>();
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		basinChecker = new DeferralBehaviour(this, this::updateBasin);
		behaviours.add(basinChecker);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		if (getSpeed() == 0)
			basinRemoved = true;
		basinChecker.scheduleUpdate();
	}

	public void gatherInputs() {
		itemInputs.clear();
		basinItemInv.ifPresent(handler -> {
			for (int slot = 0; slot < handler.getSlots(); ++slot) {
				ItemStack itemstack = handler.getStackInSlot(slot);
				if (!itemstack.isEmpty())
					itemInputs.add(itemstack);
			}
		});

		fluidInputs.clear();
		basinFluidInv.ifPresent(handler -> {
			for (int tank = 0; tank < handler.getTanks(); tank++) {
				FluidStack fluidInTank = handler.getFluidInTank(tank);
				if (!fluidInTank.isEmpty())
					fluidInputs.add(fluidInTank);
			}
		});
	}

	@Override
	public void tick() {
		if (basinRemoved) {
			basinRemoved = false;
			onBasinRemoved();
			sendData();
			return;
		}

		super.tick();
	}

	protected boolean updateBasin() {
		if (!isSpeedRequirementFulfilled())
			return true;
		if (getSpeed() == 0)
			return true;
		if (isRunning())
			return false;

		Optional<BasinTileEntity> basinTe = getBasin();
		if (!basinTe.isPresent())
			return true;
		if (!basinItemInv.isPresent())
			basinItemInv = basinTe.get()
				.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if (!basinFluidInv.isPresent())
			basinFluidInv = basinTe.get()
				.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		if (!basinFluidInv.isPresent() || !basinItemInv.isPresent())
			return true;

		if (world == null || world.isRemote)
			return true;

		gatherInputs();
		List<IRecipe<?>> recipes = getMatchingRecipes();
		if (recipes.isEmpty())
			return true;

		lastRecipe = recipes.get(0);
		startProcessingBasin();
		sendData();
		return true;
	}

	protected abstract boolean isRunning();

	public void startProcessingBasin() {}

	public boolean continueWithPreviousRecipe() {
		return true;
	}

	public void applyBasinRecipe() {
		if (lastRecipe == null)
			return;
		if (!basinItemInv.isPresent() || !basinFluidInv.isPresent())
			return;

		BasinInventory inv = (BasinInventory) basinItemInv.orElse(null);
		IItemHandlerModifiable inputs = inv.getInputHandler();
		IItemHandlerModifiable outputs = inv.getOutputHandler();
		List<ItemStack> containers = new ArrayList<>();

		NonNullList<Ingredient> ingredients = lastRecipe.getIngredients();
		Ingredients: for (int i = 0; i < ingredients.size(); i++) {
			Ingredient ingredient = ingredients.get(i);

			for (int slot = 0; slot < inputs.getSlots(); slot++) {
				if (!ingredient.test(inputs.extractItem(slot, 1, true)))
					continue;
				ItemStack extracted = inputs.extractItem(slot, 1, false);
				if (extracted.hasContainerItem())
					containers.add(extracted.getContainerItem()
						.copy());
				continue Ingredients;
			}

			// something wasn't found
			return;
		}

		if (world != null && !world.isRemote) {
			SimpleTrigger trigger = AllTriggers.MIXER_MIX;
			if (AllTileEntities.MECHANICAL_PRESS.is(this))
				trigger = AllTriggers.PRESS_COMPACT;
			AllTriggers.triggerForNearbyPlayers(trigger, world, pos, 4);
		}

		ItemHandlerHelper.insertItemStacked(outputs, lastRecipe.getRecipeOutput()
			.copy(), false); // TODO only works for single item output
		containers.forEach(stack -> ItemHandlerHelper.insertItemStacked(outputs, stack, false));

		// Continue mixing
		gatherInputs();
		if (matchBasinRecipe(lastRecipe)) {
			continueWithPreviousRecipe();
			sendData();
		}

		getBasin().ifPresent(te -> te.contentsChanged = true);
	}

	protected List<IRecipe<?>> getMatchingRecipes() {
		List<IRecipe<?>> list = RecipeFinder.get(getRecipeCacheKey(), world, this::matchStaticFilters);
		return list.stream()
			.filter(this::matchBasinRecipe)
			.sorted((r1, r2) -> r2.getIngredients()
				.size()
				- r1.getIngredients()
					.size())
			.collect(Collectors.toList());
	}

	protected abstract void onBasinRemoved();

	protected Optional<BasinTileEntity> getBasin() {
		if (world == null)
			return Optional.empty();
		TileEntity basinTE = world.getTileEntity(pos.down(2));
		if (!(basinTE instanceof BasinTileEntity))
			return Optional.empty();
		return Optional.of((BasinTileEntity) basinTE);
	}

	protected abstract <C extends IInventory> boolean matchStaticFilters(IRecipe<C> recipe);

	protected <C extends IInventory> boolean matchBasinRecipe(IRecipe<C> recipe) {
		if (recipe == null)
			return false;

		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent())
			return false;
		BasinTileEntity basinTileEntity = basin.get();
		if (!basinTileEntity.getFilter()
			.test(recipe.getRecipeOutput()))
			return false;

		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		if (!ingredients.stream()
			.allMatch(ingredient -> (ingredient.isSimple() || ingredient.getMatchingStacks().length == 1)))
			return false;
		return true;
	}

	protected abstract Object getRecipeCacheKey();

}
