package com.simibubi.create.content.contraptions.processing;

import static com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class InWorldProcessing {

	private static final RecipeWrapper WRAPPER = new RecipeWrapper(new ItemStackHandler(1));
	private static final SplashingWrapper SPLASHING_WRAPPER = new SplashingWrapper();

	public enum Type {
		SMOKING, BLASTING, SPLASHING, NONE

		;

		public static Type byBlock(IBlockReader reader, BlockPos pos) {
			BlockState blockState = reader.getBlockState(pos);
			FluidState fluidState = reader.getFluidState(pos);
			if (fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER)
				return Type.SPLASHING;
			Block block = blockState.getBlock();
			if (block == Blocks.FIRE || AllBlocks.LIT_BLAZE_BURNER.has(blockState)
				|| (BlockTags.CAMPFIRES.contains(block) && blockState.getOptionalValue(CampfireBlock.LIT).orElse(false))
				|| getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING)
				return Type.SMOKING;
			if (block == Blocks.LAVA || getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
				return Type.BLASTING;
			return Type.NONE;
		}
	}

	public static boolean canProcess(ItemEntity entity, Type type) {
		if (entity.getPersistentData()
			.contains("CreateData")) {
			CompoundNBT compound = entity.getPersistentData()
				.getCompound("CreateData");
			if (compound.contains("Processing")) {
				CompoundNBT processing = compound.getCompound("Processing");

				if (Type.valueOf(processing.getString("Type")) != type) {
					boolean canProcess = canProcess(entity.getItem(), type, entity.level);
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
		return canProcess(entity.getItem(), type, entity.level);
	}

	private static boolean canProcess(ItemStack stack, Type type, World world) {
		if (type == Type.BLASTING) {
			WRAPPER.setItem(0, stack);
			Optional<FurnaceRecipe> smeltingRecipe = world.getRecipeManager()
				.getRecipeFor(IRecipeType.SMELTING, WRAPPER, world);

			if (smeltingRecipe.isPresent())
				return true;

			WRAPPER.setItem(0, stack);
			Optional<BlastingRecipe> blastingRecipe = world.getRecipeManager()
				.getRecipeFor(IRecipeType.BLASTING, WRAPPER, world);

			if (blastingRecipe.isPresent())
				return true;

			return !stack.getItem().isFireResistant();
		}

		if (type == Type.SMOKING) {
			WRAPPER.setItem(0, stack);
			Optional<SmokingRecipe> recipe = world.getRecipeManager()
				.getRecipeFor(IRecipeType.SMOKING, WRAPPER, world);
			return recipe.isPresent();
		}

		if (type == Type.SPLASHING)
			return isWashable(stack, world);

		return false;
	}

	public static boolean isWashable(ItemStack stack, World world) {
		SPLASHING_WRAPPER.setItem(0, stack);
		Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(SPLASHING_WRAPPER, world);
		return recipe.isPresent();
	}

	public static void applyProcessing(ItemEntity entity, Type type) {
		if (decrementProcessingTime(entity, type) != 0)
			return;
		List<ItemStack> stacks = process(entity.getItem(), type, entity.level);
		if (stacks == null)
			return;
		if (stacks.isEmpty()) {
			entity.remove();
			return;
		}
		entity.setItem(stacks.remove(0));
		for (ItemStack additional : stacks) {
			ItemEntity entityIn = new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setDeltaMovement(entity.getDeltaMovement());
			entity.level.addFreshEntity(entityIn);
		}
	}

	public static TransportedResult applyProcessing(TransportedItemStack transported, World world, Type type) {
		TransportedResult ignore = TransportedResult.doNothing();
		if (transported.processedBy != type) {
			transported.processedBy = type;
			int timeModifierForStackSize = ((transported.stack.getCount() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.SERVER.kinetics.inWorldProcessingTime.get() * timeModifierForStackSize) + 1;
			transported.processingTime = processingTime;
			if (!canProcess(transported.stack, type, world))
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

	private static List<ItemStack> process(ItemStack stack, Type type, World world) {
		if (type == Type.SPLASHING) {
			SPLASHING_WRAPPER.setItem(0, stack);
			Optional<SplashingRecipe> recipe = AllRecipeTypes.SPLASHING.find(SPLASHING_WRAPPER, world);
			if (recipe.isPresent())
				return applyRecipeOn(stack, recipe.get());
			return null;
		}

		WRAPPER.setItem(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
			.getRecipeFor(IRecipeType.SMOKING, WRAPPER, world);

		if (type == Type.BLASTING) {
			if (!smokingRecipe.isPresent()) {
				WRAPPER.setItem(0, stack);
				Optional<FurnaceRecipe> smeltingRecipe = world.getRecipeManager()
					.getRecipeFor(IRecipeType.SMELTING, WRAPPER, world);

				if (smeltingRecipe.isPresent())
					return applyRecipeOn(stack, smeltingRecipe.get());

				WRAPPER.setItem(0, stack);
				Optional<BlastingRecipe> blastingRecipe = world.getRecipeManager()
					.getRecipeFor(IRecipeType.BLASTING, WRAPPER, world);

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
			ItemEntity entityIn = new ItemEntity(entity.level, entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setDeltaMovement(entity.getDeltaMovement());
			entity.level.addFreshEntity(entityIn);
		}
	}

	public static List<ItemStack> applyRecipeOn(ItemStack stackIn, IRecipe<?> recipe) {
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

	public static void spawnParticlesForProcessing(@Nullable World world, Vector3d vec, Type type) {
		if (world == null || !world.isClientSide)
			return;
		if (world.random.nextInt(8) != 0)
			return;

		switch (type) {
		case BLASTING:
			world.addParticle(ParticleTypes.LARGE_SMOKE, vec.x, vec.y + .25f, vec.z, 0, 1 / 16f, 0);
			break;
		case SMOKING:
			world.addParticle(ParticleTypes.POOF, vec.x, vec.y + .25f, vec.z, 0, 1 / 16f, 0);
			break;
		case SPLASHING:
			Vector3d color = ColorHelper.getRGB(0x0055FF);
			world.addParticle(new RedstoneParticleData((float) color.x, (float) color.y, (float) color.z, 1),
				vec.x + (world.random.nextFloat() - .5f) * .5f, vec.y + .5f, vec.z + (world.random.nextFloat() - .5f) * .5f,
				0, 1 / 8f, 0);
			world.addParticle(ParticleTypes.SPIT, vec.x + (world.random.nextFloat() - .5f) * .5f, vec.y + .5f,
				vec.z + (world.random.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
			break;
		default:
			break;
		}
	}

	public static class SplashingWrapper extends RecipeWrapper {
		public SplashingWrapper() {
			super(new ItemStackHandler(1));
		}
	}

}
