package com.simibubi.create.content.logistics;

import static com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.processing.ProcessingRecipe;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.BlastFurnaceTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
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

		;

		public static Type byBlock(IBlockReader reader, BlockPos pos) {
			BlockState blockState = reader.getBlockState(pos);
			IFluidState fluidState = reader.getFluidState(pos);
			if (fluidState.getFluid() == Fluids.WATER || fluidState.getFluid() == Fluids.FLOWING_WATER)
				return Type.SPLASHING;
			if (blockState.getBlock() == Blocks.FIRE
				|| (blockState.getBlock() == Blocks.CAMPFIRE && blockState.get(CampfireBlock.LIT))
				|| getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING)
				return Type.SMOKING;
			if (blockState.getBlock() == Blocks.LAVA
				|| getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING))
				return Type.BLASTING;
			return null;
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
			// FIXME this does not need to be a TE
			SmokerTileEntity smoker = new SmokerTileEntity();
			smoker.setLocation(world, BlockPos.ZERO);
			smoker.setInventorySlotContents(0, stack);
			Optional<SmokingRecipe> recipe = world.getRecipeManager()
				.getRecipe(IRecipeType.SMOKING, smoker, world);
			return recipe.isPresent();
		}

		if (type == Type.SPLASHING)
			return isWashable(stack, world);

		return false;
	}

	public static boolean isWashable(ItemStack stack, World world) {
		splashingInv.setInventorySlotContents(0, stack);
		Optional<SplashingRecipe> recipe = world.getRecipeManager()
			.getRecipe(AllRecipeTypes.SPLASHING.getType(), splashingInv, world);
		return recipe.isPresent();
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
			ItemEntity entityIn = new ItemEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), additional);
			entityIn.setMotion(entity.getMotion());
			entity.world.addEntity(entityIn);
		}
	}

	public static List<TransportedItemStack> applyProcessing(TransportedItemStack transported, World world, Type type) {
		if (transported.processedBy != type) {
			transported.processedBy = type;
			int timeModifierForStackSize = ((transported.stack.getCount() - 1) / 16) + 1;
			int processingTime =
				(int) (AllConfigs.SERVER.kinetics.inWorldProcessingTime.get() * timeModifierForStackSize) + 1;
			transported.processingTime = processingTime;
			if (!canProcess(transported.stack, type, world))
				transported.processingTime = -1;
			return null;
		}
		if (transported.processingTime == -1)
			return null;
		if (transported.processingTime-- > 0)
			return null;

		List<ItemStack> stacks = process(transported.stack, type, world);
		if (stacks == null)
			return null;

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
			Optional<SplashingRecipe> recipe = world.getRecipeManager()
				.getRecipe(AllRecipeTypes.SPLASHING.getType(), splashingInv, world);
			if (recipe.isPresent())
				return applyRecipeOn(stack, recipe.get());
			return null;
		}

		// FIXME this does not need to be a TE
		SmokerTileEntity smoker = new SmokerTileEntity();
		smoker.setLocation(world, BlockPos.ZERO);
		smoker.setInventorySlotContents(0, stack);
		Optional<SmokingRecipe> smokingRecipe = world.getRecipeManager()
			.getRecipe(IRecipeType.SMOKING, smoker, world);

		if (type == Type.BLASTING) {
			// FIXME this does not need to be a TE
			FurnaceTileEntity furnace = new FurnaceTileEntity();
			furnace.setLocation(world, BlockPos.ZERO);
			furnace.setInventorySlotContents(0, stack);
			Optional<FurnaceRecipe> smeltingRecipe = world.getRecipeManager()
				.getRecipe(IRecipeType.SMELTING, furnace, world);

			if (!smokingRecipe.isPresent()) {
				if (smeltingRecipe.isPresent())
					return applyRecipeOn(stack, smeltingRecipe.get());

				// FIXME this does not need to be a TE
				BlastFurnaceTileEntity blastFurnace = new BlastFurnaceTileEntity();
				blastFurnace.setLocation(world, BlockPos.ZERO);
				blastFurnace.setInventorySlotContents(0, stack);
				Optional<BlastingRecipe> blastingRecipe = world.getRecipeManager()
					.getRecipe(IRecipeType.BLASTING, blastFurnace, world);

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
			ItemEntity entityIn = new ItemEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), additional);
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
			ItemStack out = recipe.getRecipeOutput()
				.copy();
			stacks = ItemHelper.multipliedOutput(stackIn, out);
		}

		return stacks;
	}

	public static void spawnParticlesForProcessing(World world, Vec3d vec, Type type) {
		if (!world.isRemote)
			return;
		if (world.rand.nextInt(8) != 0)
			return;

		switch (type) {
		case BLASTING:
			world.addParticle(ParticleTypes.LARGE_SMOKE, vec.x, vec.y + .25f, vec.z, 0, 1 / 16f, 0);
			break;
		case SMOKING:
			world.addParticle(ParticleTypes.POOF, vec.x, vec.y + .25f, vec.z, 0, 1 / 16f, 0);
			break;
		case SPLASHING:
			Vec3d color = ColorHelper.getRGB(0x0055FF);
			world.addParticle(new RedstoneParticleData((float) color.x, (float) color.y, (float) color.z, 1),
				vec.x + (world.rand.nextFloat() - .5f) * .5f, vec.y + .5f, vec.z + (world.rand.nextFloat() - .5f) * .5f,
				0, 1 / 8f, 0);
			world.addParticle(ParticleTypes.SPIT, vec.x + (world.rand.nextFloat() - .5f) * .5f, vec.y + .5f,
				vec.z + (world.rand.nextFloat() - .5f) * .5f, 0, 1 / 8f, 0);
			break;
		default:
			break;
		}
	}

	public static boolean isFrozen() {
		return AllConfigs.SERVER.control.freezeInWorldProcessing.get();
	}

}
