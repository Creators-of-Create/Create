package com.simibubi.create.modules.logistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.utility.ItemHelper;
import com.simibubi.create.modules.contraptions.receivers.SplashingRecipe;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BlastFurnaceTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InWorldProcessing {

	public static class SplashingInv extends RecipeWrapper {
		public SplashingInv() {
			super(new ItemStackHandler(1));
		}
	}

	public static SplashingInv splashingInv = new SplashingInv();

	public enum Type {
		SMOKING, BLASTING, SPLASHING
	}

	public static boolean canProcess(ItemEntity entity, Type type) {
		World world = entity.world;

		if (entity.getPersistantData().contains("CreateData")
				&& entity.getPersistantData().getCompound("CreateData").contains("Processing"))
			return true;

		if (type == Type.BLASTING) {
			return true;
		}

		if (type == Type.SMOKING) {
			SmokerTileEntity smoker = new SmokerTileEntity();
			smoker.setWorld(world);
			smoker.setInventorySlotContents(0, entity.getItem());
			Optional<SmokingRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMOKING, smoker, world);
			return recipe.isPresent();
		}

		if (type == Type.SPLASHING) {
			splashingInv.setInventorySlotContents(0, entity.getItem());
			Optional<SplashingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.SPLASHING,
					splashingInv, world);
			return recipe.isPresent();
		}

		return false;
	}

	public static void process(ItemEntity entity, Type type) {
		World world = entity.world;
		if (decrementProcessingTime(entity, type) != 0)
			return;

		if (type == Type.SPLASHING) {
			splashingInv.setInventorySlotContents(0, entity.getItem());
			Optional<SplashingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.SPLASHING,
					splashingInv, world);
			if (recipe.isPresent())
				applyRecipeOn(entity, recipe.get());
			return;
		}

		SmokerTileEntity smoker = new SmokerTileEntity();
		smoker.setWorld(world);
		smoker.setInventorySlotContents(0, entity.getItem());
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager().getRecipe(IRecipeType.SMOKING, smoker, world);

		if (type == Type.BLASTING) {
			FurnaceTileEntity furnace = new FurnaceTileEntity();
			furnace.setWorld(world);
			furnace.setInventorySlotContents(0, entity.getItem());
			Optional<FurnaceRecipe> smeltingRecipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, furnace,
					world);

			if (!smokingRecipe.isPresent()) {
				if (smeltingRecipe.isPresent()) {
					applyRecipeOn(entity, smeltingRecipe.get());
					return;
				}

				BlastFurnaceTileEntity blastFurnace = new BlastFurnaceTileEntity();
				blastFurnace.setWorld(world);
				blastFurnace.setInventorySlotContents(0, entity.getItem());
				Optional<BlastingRecipe> blastingRecipe = world.getRecipeManager().getRecipe(IRecipeType.BLASTING,
						blastFurnace, world);

				if (blastingRecipe.isPresent()) {
					applyRecipeOn(entity, blastingRecipe.get());
					return;
				}
			}

			entity.remove();
			return;
		}

		if (type == Type.SMOKING && smokingRecipe.isPresent()) {
			applyRecipeOn(entity, smokingRecipe.get());
			return;
		}

	}

	private static int decrementProcessingTime(ItemEntity entity, Type type) {
		CompoundNBT nbt = entity.getPersistantData();

		if (!nbt.contains("CreateData"))
			nbt.put("CreateData", new CompoundNBT());
		CompoundNBT createData = nbt.getCompound("CreateData");

		if (!createData.contains("Processing"))
			createData.put("Processing", new CompoundNBT());
		CompoundNBT processing = createData.getCompound("Processing");

		if (!processing.contains("Type") || Type.valueOf(processing.getString("Type")) != type) {
			processing.putString("Type", type.name());
			processing.putInt("Time", CreateConfig.parameters.inWorldProcessingTime.get() + 1);
		}

		int value = processing.getInt("Time") - 1;
		processing.putInt("Time", value);
		return value;
	}

	public static void applyRecipeOn(ItemEntity entity, IRecipe<?> recipe) {
		List<ItemStack> stacks;

		if (recipe instanceof SplashingRecipe) {
			stacks = new ArrayList<>();
			for (int i = 0; i < entity.getItem().getCount(); i++) {
				for (ItemStack stack : ((SplashingRecipe) recipe).rollResults()) {
					for (ItemStack previouslyRolled : stacks) {
						if (stack.isEmpty())
							continue;
						if (!ItemHandlerHelper.canItemStacksStack(stack, previouslyRolled))
							continue;
						int amount = Math.min(previouslyRolled.getMaxStackSize() - previouslyRolled.getCount(),
								stack.getCount());
						previouslyRolled.grow(amount);
						stack.shrink(amount);
					}

					if (stack.isEmpty())
						continue;

					stacks.add(stack);
				}
			}
		} else {
			ItemStack out = recipe.getRecipeOutput().copy();
			stacks = ItemHelper.multipliedOutput(entity.getItem(), out);
		}

		if (stacks.isEmpty()) {
			entity.remove();
			return;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks)
			entity.world.addEntity(new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, additional));
	}

	public static boolean isFrozen() {
		return CreateConfig.parameters.freezeInWorldProcessing.get();
	}

}
