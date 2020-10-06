package com.simibubi.create.content.contraptions.processing;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.advancement.ITriggerable;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class BasinOperatingTileEntity extends KineticTileEntity {

	public DeferralBehaviour basinChecker;
	public boolean basinRemoved;
	protected IRecipe<?> currentRecipe;

	public BasinOperatingTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
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
		basinRemoved = false;
		basinChecker.scheduleUpdate();
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
		if (world == null || world.isRemote)
			return true;

		List<IRecipe<?>> recipes = getMatchingRecipes();
		if (recipes.isEmpty())
			return true;
		currentRecipe = recipes.get(0);
		startProcessingBasin();
		sendData();
		return true;
	}

	protected abstract boolean isRunning();

	public void startProcessingBasin() {}

	public boolean continueWithPreviousRecipe() {
		return true;
	}

	protected <C extends IInventory> boolean matchBasinRecipe(IRecipe<C> recipe) {
		if (recipe == null)
			return false;
		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.isPresent())
			return false;
		return BasinRecipe.match(basin.get(), recipe);
	}
	
	protected void applyBasinRecipe() {
		if (currentRecipe == null)
			return;
		
		Optional<BasinTileEntity> optionalBasin = getBasin();
		if (!optionalBasin.isPresent())
			return;
		BasinTileEntity basin = optionalBasin.get();
		if (!BasinRecipe.apply(basin, currentRecipe))
			return;
		Optional<ITriggerable> processedRecipeTrigger = getProcessedRecipeTrigger();
		if (world != null && !world.isRemote && processedRecipeTrigger.isPresent()) 
			AllTriggers.triggerForNearbyPlayers(processedRecipeTrigger.get(), world, pos, 4);
		basin.inputTank.sendDataImmediately();
	
		// Continue mixing
		if (matchBasinRecipe(currentRecipe)) {
			continueWithPreviousRecipe();
			sendData();
		}

		basin.notifyChangeOfContents();
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
	
	protected Optional<ITriggerable> getProcessedRecipeTrigger() {
		return Optional.empty();
	}

	protected abstract <C extends IInventory> boolean matchStaticFilters(IRecipe<C> recipe);

	protected abstract Object getRecipeCacheKey();

}
