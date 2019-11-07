package com.simibubi.create.modules.contraptions.receivers;

import static com.simibubi.create.modules.contraptions.receivers.SawBlock.RUNNING;

import java.util.Optional;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.entity.item.ItemEntity;

public class SawTileEntity extends KineticTileEntity {

	public ProcessingInventory inventory;

	public SawTileEntity() {
		super(AllTileEntities.SAW.type);
		inventory = new ProcessingInventory();
	}

	@Override
	public void onSpeedChanged() {
		boolean shouldRun = Math.abs(getSpeed()) > 1 / 64f;
		boolean running = getBlockState().get(RUNNING);
		if (shouldRun != running)
			world.setBlockState(pos, getBlockState().with(RUNNING, shouldRun));
	}

	@Override
	public void tick() {
		super.tick();

	}

	public void insertItem(ItemEntity entity) {
		if (!inventory.isEmpty())
			return;

		inventory.clear();
		inventory.setInventorySlotContents(0, entity.getItem().copy());
		Optional<CuttingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.CUTTING, inventory, world);

		inventory.processingDuration = recipe.isPresent() ? recipe.get().getProcessingDuration() : 100;
		inventory.appliedRecipe = false;
		entity.remove();
	}

}
