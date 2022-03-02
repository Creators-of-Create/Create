package com.simibubi.create.content.contraptions.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.processing.fan.AbstractFanProcessingType;
import com.simibubi.create.content.contraptions.processing.fan.HauntingRecipe;
import com.simibubi.create.content.contraptions.processing.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InWorldProcessing {

	public static final DamageSource FIRE_DAMAGE_SOURCE = new DamageSource("create.fan_fire").setScalesWithDifficulty()
			.setIsFire();
	public static final DamageSource LAVA_DAMAGE_SOURCE = new DamageSource("create.fan_lava").setScalesWithDifficulty()
			.setIsFire();

	public static final RecipeWrapper RECIPE_WRAPPER = new RecipeWrapper(new ItemStackHandler(1));

	public static boolean canProcess(ItemEntity entity, AbstractFanProcessingType type) {
		if (entity.getPersistentData()
				.contains("CreateData")) {
			CompoundTag compound = entity.getPersistentData()
					.getCompound("CreateData");
			if (compound.contains("Processing")) {
				CompoundTag processing = compound.getCompound("Processing");

				if (AbstractFanProcessingType.valueOf(processing.getString("Type")) != type) {
					boolean canProcess = type.canProcess(entity.getItem(), entity.level);
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
		return type.canProcess(entity.getItem(), entity.level);
	}

	public static boolean isWashable(ItemStack stack, Level world) {
		RECIPE_WRAPPER.setItem(0, stack);
		Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(RECIPE_WRAPPER, world);
		return recipe.isPresent();
	}

	public static boolean isHauntable(ItemStack stack, Level world) {
		RECIPE_WRAPPER.setItem(0, stack);
		Optional<HauntingRecipe> recipe = AllRecipeTypes.HAUNTING.find(RECIPE_WRAPPER, world);
		return recipe.isPresent();
	}

	public static void applyProcessing(ItemEntity entity, AbstractFanProcessingType type) {
		if (decrementProcessingTime(entity, type) != 0)
			return;
		List<ItemStack> stacks = process(entity.getItem(), type, entity.level);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.discard();
			return;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setDeltaMovement(entity.getDeltaMovement());
			entity.level.addFreshEntity(entityIn);
		}
	}

	public static TransportedResult applyProcessing(TransportedItemStack transported, Level world, AbstractFanProcessingType type) {
		TransportedResult ignore = TransportedResult.doNothing();
		if (transported.processedBy != type) {
			transported.processedBy = type;
			int timeModifierForStackSize = ((transported.stack.getCount() - 1) / 16) + 1;
			int processingTime =
					(int) (AllConfigs.SERVER.kinetics.inWorldProcessingTime.get() * timeModifierForStackSize) + 1;
			transported.processingTime = processingTime;
			if (!type.canProcess(transported.stack, world))
				transported.processingTime = -1;
			return ignore;
		}
		if (transported.processingTime == -1)
			return ignore;
		if (transported.processingTime-- > 0)
			return ignore;

		List<ItemStack> stacks = process(transported.stack, type, world);
		if (stacks == null)
			return ignore;

		List<TransportedItemStack> transportedStacks = new ArrayList<>();
		for (ItemStack additional : stacks) {
			TransportedItemStack newTransported = transported.getSimilar();
			newTransported.stack = additional.copy();
			transportedStacks.add(newTransported);
		}
		return TransportedResult.convertTo(transportedStacks);
	}

	private static List<ItemStack> process(ItemStack stack, AbstractFanProcessingType type, Level world) {
		return type.process(stack, world);
	}

	private static int decrementProcessingTime(ItemEntity entity, AbstractFanProcessingType type) {
		CompoundTag nbt = entity.getPersistentData();

		if (!nbt.contains("CreateData"))
			nbt.put("CreateData", new CompoundTag());
		CompoundTag createData = nbt.getCompound("CreateData");

		if (!createData.contains("Processing"))
			createData.put("Processing", new CompoundTag());
		CompoundTag processing = createData.getCompound("Processing");

		if (!processing.contains("Type") || AbstractFanProcessingType.valueOf(processing.getString("Type")) != type) {
			processing.putString("Type", type.name());
			int timeModifierForStackSize = ((entity.getItem()
					.getCount() - 1) / 16) + 1;
			int processingTime =
					(int) (AllConfigs.SERVER.kinetics.inWorldProcessingTime.get() * timeModifierForStackSize) + 1;
			processing.putInt("Time", processingTime);
		}

		int value = processing.getInt("Time") - 1;
		processing.putInt("Time", value);
		return value;
	}

	public static void applyRecipeOn(ItemEntity entity, Recipe<?> recipe) {
		List<ItemStack> stacks = applyRecipeOn(entity.getItem(), recipe);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.discard();
			return;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setDeltaMovement(entity.getDeltaMovement());
			entity.level.addFreshEntity(entityIn);
		}
	}

	public static List<ItemStack> applyRecipeOn(ItemStack stackIn, Recipe<?> recipe) {
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
			ItemStack out = recipe.getResultItem()
					.copy();
			stacks = ItemHelper.multipliedOutput(stackIn, out);
		}

		return stacks;
	}


}
