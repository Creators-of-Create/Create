package com.simibubi.create.modules.logistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipes;
import com.simibubi.create.CreateConfig;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.modules.contraptions.base.ProcessingRecipe;
import com.simibubi.create.modules.contraptions.receivers.SplashingRecipe;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.modules.contraptions.relays.belt.TransportedItemStack;

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
		if (entity.getPersistentData().contains("CreateData")) {
			CompoundNBT compound = entity.getPersistentData().getCompound("CreateData");
			if (compound.contains("Processing")) {
				CompoundNBT processing = compound.getCompound("Processing");

				if (Type.valueOf(processing.getString("Type")) != type) {
					boolean canProcess = canProcess(entity.getItem(), type, entity.world);
					processing.putString("Type", type.name());
					if (!canProcess)
						processing.putInt("Time", -1);
					return canProcess;
				} else if (processing.getInt("Time") >= 0)
					return true;
				else if (processing.getInt("Time") == -1)
					return false;
			}
		}
		return canProcess(entity.getItem(), type, entity.world);
	}

	private static boolean canProcess(ItemStack stack, Type type, World world) {
		if (type == Type.BLASTING) {
			return true;
		}

		if (type == Type.SMOKING) {
			SmokerTileEntity smoker = new SmokerTileEntity();
			smoker.setWorld(world);
			smoker.setInventorySlotContents(0, stack);
			Optional<SmokingRecipe> recipe = world.getRecipeManager().getRecipe(IRecipeType.SMOKING, smoker, world);
			return recipe.isPresent();
		}

		if (type == Type.SPLASHING) {
			splashingInv.setInventorySlotContents(0, stack);
			Optional<SplashingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.SPLASHING,
					splashingInv, world);
			return recipe.isPresent();
		}

		return false;
	}

	public static void applyProcessing(ItemEntity entity, Type type) {
		if (decrementProcessingTime(entity, type) != 0)
			return;
		List<ItemStack> stacks = process(entity.getItem(), type, entity.world);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.remove();
			return;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, additional);
			entityIn.setMotion(entity.getMotion());
			entity.world.addEntity(entityIn);
		}
	}

	public static List<TransportedItemStack> applyProcessing(TransportedItemStack transported, BeltTileEntity belt, Type type) {
		if (transported.processedBy != type) {
			transported.processedBy = type;
			transported.processingTime = CreateConfig.parameters.inWorldProcessingTime.get() + 1;
			if (!canProcess(transported.stack, type, belt.getWorld()))
				transported.processingTime = -1;
			return null;
		}
		if (transported.processingTime == -1)
			return null;
		if (transported.processingTime-- > 0)
			return null;

		List<ItemStack> stacks = process(transported.stack, type, belt.getWorld());
		List<TransportedItemStack> transportedStacks = new ArrayList<>();
		for (ItemStack additional : stacks) {
			TransportedItemStack newTransported = transported.getSimilar();
			newTransported.stack = additional.copy();
			transportedStacks.add(newTransported);
		}
		return transportedStacks;
	}

	private static List<ItemStack> process(ItemStack stack, Type type, World world) {
		if (type == Type.SPLASHING) {
			splashingInv.setInventorySlotContents(0, stack);
			Optional<SplashingRecipe> recipe = world.getRecipeManager().getRecipe(AllRecipes.Types.SPLASHING,
					splashingInv, world);
			if (recipe.isPresent())
				return applyRecipeOn(stack, recipe.get());
			return null;
		}

		SmokerTileEntity smoker = new SmokerTileEntity();
		smoker.setWorld(world);
		smoker.setInventorySlotContents(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager().getRecipe(IRecipeType.SMOKING, smoker, world);

		if (type == Type.BLASTING) {
			FurnaceTileEntity furnace = new FurnaceTileEntity();
			furnace.setWorld(world);
			furnace.setInventorySlotContents(0, stack);
			Optional<FurnaceRecipe> smeltingRecipe = world.getRecipeManager().getRecipe(IRecipeType.SMELTING, furnace,
					world);

			if (!smokingRecipe.isPresent()) {
				if (smeltingRecipe.isPresent())
					return applyRecipeOn(stack, smeltingRecipe.get());

				BlastFurnaceTileEntity blastFurnace = new BlastFurnaceTileEntity();
				blastFurnace.setWorld(world);
				blastFurnace.setInventorySlotContents(0, stack);
				Optional<BlastingRecipe> blastingRecipe = world.getRecipeManager().getRecipe(IRecipeType.BLASTING,
						blastFurnace, world);

				if (blastingRecipe.isPresent())
					return applyRecipeOn(stack, blastingRecipe.get());
			}

			return Collections.emptyList();
		}

		if (type == Type.SMOKING && smokingRecipe.isPresent())
			return applyRecipeOn(stack, smokingRecipe.get());

		return null;
	}

	private static int decrementProcessingTime(ItemEntity entity, Type type) {
		CompoundNBT nbt = entity.getPersistentData();

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
		List<ItemStack> stacks = applyRecipeOn(entity.getItem(), recipe);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.remove();
			return;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, additional);
			entityIn.setMotion(entity.getMotion());
			entity.world.addEntity(entityIn);
		}
	}

	private static List<ItemStack> applyRecipeOn(ItemStack stackIn, IRecipe<?> recipe) {
		List<ItemStack> stacks;

		if (recipe instanceof ProcessingRecipe) {
			stacks = new ArrayList<>();
			for (int i = 0; i < stackIn.getCount(); i++) {
				List<ItemStack> rollResults = ((ProcessingRecipe<?>) recipe).rollResults();
				for (ItemStack stack : rollResults) {
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
			stacks = ItemHelper.multipliedOutput(stackIn, out);
		}

		return stacks;
	}

	public static boolean isFrozen() {
		return CreateConfig.parameters.freezeInWorldProcessing.get();
	}

}
