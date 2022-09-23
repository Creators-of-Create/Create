package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.simple.DeferralBehaviour;
import com.simibubi.create.foundation.utility.recipe.RecipeFinder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BasinOperatingTileEntity extends KineticTileEntity {

	public DeferralBehaviour basinChecker;
	public boolean basinRemoved;
	protected Recipe<?> currentRecipe;

	public BasinOperatingTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
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
			return true;
		if (level == null || level.isClientSide)
			return true;
		Optional<BasinTileEntity> basin = getBasin();
		if (!basin.filter(BasinTileEntity::canContinueProcessing)
			.isPresent())
			return true;

		List<Recipe<?>> recipes = getMatchingRecipes();
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

	protected <C extends Container> boolean matchBasinRecipe(Recipe<C> recipe) {
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
		boolean wasEmpty = basin.canContinueProcessing();
		if (!BasinRecipe.apply(basin, currentRecipe))
			return;
		getProcessedRecipeTrigger().ifPresent(this::award);
		basin.inputTank.sendDataImmediately();

		// Continue mixing
		if (wasEmpty && matchBasinRecipe(currentRecipe)) {
			continueWithPreviousRecipe();
			sendData();
		}

		basin.notifyChangeOfContents();
	}

	protected List<Recipe<?>> getMatchingRecipes() {
		if (getBasin().map(BasinTileEntity::isEmpty)
			.orElse(true))
			return new ArrayList<>();
		
		List<Recipe<?>> list = RecipeFinder.get(getRecipeCacheKey(), level, this::matchStaticFilters);
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
		if (level == null)
			return Optional.empty();
		BlockEntity basinTE = level.getBlockEntity(worldPosition.below(2));
		if (!(basinTE instanceof BasinTileEntity))
			return Optional.empty();
		return Optional.of((BasinTileEntity) basinTE);
	}

	protected Optional<CreateAdvancement> getProcessedRecipeTrigger() {
		return Optional.empty();
	}

	protected abstract <C extends Container> boolean matchStaticFilters(Recipe<C> recipe);

	protected abstract Object getRecipeCacheKey();
}
