package com.simibubi.create.content.contraptions.processing;

import static com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.getHeatLevelOf;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.mixin.CancelEntityRenderMixin;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.ColorHelper;

import com.simibubi.create.foundation.utility.Pair;

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
import net.minecraft.util.ResourceLocation;
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

	protected static HashMap<Block, BiFunction<IBlockReader, BlockPos, Type>> fanHandlers = new HashMap<>();
	protected static List<Pair<BiPredicate<IBlockReader, BlockPos> ,BiFunction<IBlockReader, BlockPos, Type>>> customFanHandlers = new ArrayList<>();

	public enum Type {
		SMOKING, BLASTING, SPLASHING, NONE

		;

		public static void addTypeHandler(Block block, BiFunction<IBlockReader, BlockPos, Type> function) {
			fanHandlers.put(block, function);
		}

		public static void addCustomTypeHandler(BiPredicate<IBlockReader, BlockPos> predicate, BiFunction<IBlockReader, BlockPos, Type> function) {
			customFanHandlers.add(Pair.of(predicate, function));
		}

		static {
			addTypeHandler(Blocks.CAMPFIRE, (block, pos) -> {
				BlockState state = block.getBlockState(pos);
				if(state.getOptionalValue(CampfireBlock.LIT).orElse(false)) {
					return Type.SMOKING;
				}
				return null;
			});
		}

		public static Type byBlock(IBlockReader reader, BlockPos pos) {
			BlockState blockState = reader.getBlockState(pos);
			Block block = blockState.getBlock();
			Set<ResourceLocation> tags = block.getTags();
			if(fanHandlers.containsKey(block)) {
				Type r = fanHandlers.get(block).apply(reader, pos);
				if(r != null) return r;
			}
			if (tags.contains(AllTags.AllBlockTags.FAN_SPLASHING.tag.getName()))
				return Type.SPLASHING;
			boolean ignoreHeatLevel = tags.contains(AllTags.AllBlockTags.FAN_IGNORE_HEAT_LEVEL.tag.getName());
			if (tags.contains(AllTags.AllBlockTags.FAN_SMOKING.tag.getName()) ||
					(!ignoreHeatLevel && getHeatLevelOf(blockState) == BlazeBurnerBlock.HeatLevel.SMOULDERING))
				return Type.SMOKING;
			if (tags.contains(AllTags.AllBlockTags.FAN_BLASTING.tag.getName()) ||
					(!ignoreHeatLevel && getHeatLevelOf(blockState).isAtLeast(BlazeBurnerBlock.HeatLevel.FADING)))
				return Type.BLASTING;
			for(Pair<BiPredicate<IBlockReader, BlockPos>, BiFunction<IBlockReader, BlockPos, Type>> pair : customFanHandlers) {
				if(pair.getFirst().test(reader, pos)) {
					Type r = pair.getSecond().apply(reader, pos);
					if(r != null) return r;
				}
			}
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
