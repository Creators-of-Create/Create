package com.simibubi.create.foundation.utility;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.IMergeableBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

public class BlockHelper {

	public static BlockState setZeroAge(BlockState blockState) {
		if (blockState.hasProperty(BlockStateProperties.AGE_1))
			return blockState.setValue(BlockStateProperties.AGE_1, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_2))
			return blockState.setValue(BlockStateProperties.AGE_2, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_3))
			return blockState.setValue(BlockStateProperties.AGE_3, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_5))
			return blockState.setValue(BlockStateProperties.AGE_5, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_7))
			return blockState.setValue(BlockStateProperties.AGE_7, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_15))
			return blockState.setValue(BlockStateProperties.AGE_15, 0);
		if (blockState.hasProperty(BlockStateProperties.AGE_25))
			return blockState.setValue(BlockStateProperties.AGE_25, 0);
		if (blockState.hasProperty(BlockStateProperties.LEVEL_HONEY))
			return blockState.setValue(BlockStateProperties.LEVEL_HONEY, 0);
		if (blockState.hasProperty(BlockStateProperties.HATCH))
			return blockState.setValue(BlockStateProperties.HATCH, 0);
		if (blockState.hasProperty(BlockStateProperties.STAGE))
			return blockState.setValue(BlockStateProperties.STAGE, 0);
		if (blockState.is(BlockTags.CAULDRONS))
			return Blocks.CAULDRON.defaultBlockState();
		if (blockState.hasProperty(BlockStateProperties.LEVEL_COMPOSTER))
			return blockState.setValue(BlockStateProperties.LEVEL_COMPOSTER, 0);
		if (blockState.hasProperty(BlockStateProperties.EXTENDED))
			return blockState.setValue(BlockStateProperties.EXTENDED, false);
		return blockState;
	}

	public static int findAndRemoveInInventory(BlockState block, Player player, int amount) {
		int amountFound = 0;
		Item required = getRequiredItem(block).getItem();

		boolean needsTwo = block.hasProperty(BlockStateProperties.SLAB_TYPE)
			&& block.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;

		if (needsTwo)
			amount *= 2;

		if (block.hasProperty(BlockStateProperties.EGGS))
			amount *= block.getValue(BlockStateProperties.EGGS);

		if (block.hasProperty(BlockStateProperties.PICKLES))
			amount *= block.getValue(BlockStateProperties.PICKLES);

		{
			// Try held Item first
			int preferredSlot = player.getInventory().selected;
			ItemStack itemstack = player.getInventory()
				.getItem(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.getInventory()
					.setItem(preferredSlot, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.getInventory()
			.getContainerSize(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.getInventory()
				.getItem(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.getInventory()
					.setItem(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.getInventory()
					.add(new ItemStack(required));
			amountFound /= 2;
		}

		return amountFound;
	}

	public static ItemStack getRequiredItem(BlockState state) {
		ItemStack itemStack = new ItemStack(state.getBlock());
		Item item = itemStack.getItem();
		if (item == Items.FARMLAND || item == Items.DIRT_PATH)
			itemStack = new ItemStack(Items.DIRT);
		return itemStack;
	}

	public static void destroyBlock(Level world, BlockPos pos, float effectChance) {
		destroyBlock(world, pos, effectChance, stack -> Block.popResource(world, pos, stack));
	}

	public static void destroyBlock(Level world, BlockPos pos, float effectChance,
		Consumer<ItemStack> droppedItemCallback) {
		destroyBlockAs(world, pos, null, ItemStack.EMPTY, effectChance, droppedItemCallback);
	}

	public static void destroyBlockAs(Level world, BlockPos pos, @Nullable Player player, ItemStack usedTool,
		float effectChance, Consumer<ItemStack> droppedItemCallback) {
		FluidState fluidState = world.getFluidState(pos);
		BlockState state = world.getBlockState(pos);

		if (world.random.nextFloat() < effectChance)
			world.levelEvent(2001, pos, Block.getId(state));
		BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;

		if (player != null) {
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled())
				return;

			if (event.getExpToDrop() > 0 && world instanceof ServerLevel)
				state.getBlock()
					.popExperience((ServerLevel) world, pos, event.getExpToDrop());

			usedTool.mineBlock(world, state, pos, player);
			player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
		}

		if (world instanceof ServerLevel && world.getGameRules()
			.getBoolean(GameRules.RULE_DOBLOCKDROPS) && !world.restoringBlockSnapshots
			&& (player == null || !player.isCreative())) {
			for (ItemStack itemStack : Block.getDrops(state, (ServerLevel) world, pos, blockEntity, player, usedTool))
				droppedItemCallback.accept(itemStack);

			// Simulating IceBlock#playerDestroy. Not calling method directly as it would drop item
			// entities as a side-effect
			if (state.getBlock() instanceof IceBlock && usedTool.getEnchantmentLevel(Enchantments.SILK_TOUCH) == 0) {
				if (world.dimensionType()
					.ultraWarm())
					return;

				 BlockState blockstate = world.getBlockState(pos.below());
		         if (blockstate.blocksMotion() || blockstate.liquid())
					world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
				return;
			}

			state.spawnAfterBreak((ServerLevel) world, pos, ItemStack.EMPTY, true);
		}

		world.setBlockAndUpdate(pos, fluidState.createLegacyBlock());
	}

	public static boolean isSolidWall(BlockGetter reader, BlockPos fromPos, Direction toDirection) {
		return hasBlockSolidSide(reader.getBlockState(fromPos.relative(toDirection)), reader,
			fromPos.relative(toDirection), toDirection.getOpposite());
	}

	public static boolean noCollisionInSpace(BlockGetter reader, BlockPos pos) {
		return reader.getBlockState(pos)
			.getCollisionShape(reader, pos)
			.isEmpty();
	}

	private static void placeRailWithoutUpdate(Level world, BlockState state, BlockPos target) {
		LevelChunk chunk = world.getChunkAt(target);
		int idx = chunk.getSectionIndex(target.getY());
		LevelChunkSection chunksection = chunk.getSection(idx);
		if (chunksection == null) {
			chunksection = new LevelChunkSection(world.registryAccess()
				.registryOrThrow(Registries.BIOME));
			chunk.getSections()[idx] = chunksection;
		}
		BlockState old = chunksection.setBlockState(SectionPos.sectionRelative(target.getX()),
			SectionPos.sectionRelative(target.getY()), SectionPos.sectionRelative(target.getZ()), state);
		chunk.setUnsaved(true);
		world.markAndNotifyBlock(target, chunk, old, state, 82, 512);

		world.setBlock(target, state, 82);
		world.neighborChanged(target, world.getBlockState(target.below())
			.getBlock(), target.below());
	}

	public static CompoundTag prepareBlockEntityData(BlockState blockState, BlockEntity blockEntity) {
		CompoundTag data = null;
		if (blockEntity == null)
			return data;
		if (AllBlockTags.SAFE_NBT.matches(blockState)) {
			data = blockEntity.saveWithFullMetadata();
			data = NBTProcessors.process(blockEntity, data, true);
		} else if (blockEntity instanceof IPartialSafeNBT) {
			data = new CompoundTag();
			((IPartialSafeNBT) blockEntity).writeSafe(data);
			data = NBTProcessors.process(blockEntity, data, true);
		}
		return data;
	}

	public static void placeSchematicBlock(Level world, BlockState state, BlockPos target, ItemStack stack,
		@Nullable CompoundTag data) {
		BlockEntity existingBlockEntity = world.getBlockEntity(target);

		// Piston
		if (state.hasProperty(BlockStateProperties.EXTENDED))
			state = state.setValue(BlockStateProperties.EXTENDED, Boolean.FALSE);
		if (state.hasProperty(BlockStateProperties.WATERLOGGED))
			state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);

		if (state.getBlock() == Blocks.COMPOSTER)
			state = Blocks.COMPOSTER.defaultBlockState();
		else if (state.getBlock() != Blocks.SEA_PICKLE && state.getBlock() instanceof IPlantable)
			state = ((IPlantable) state.getBlock()).getPlant(world, target);
		else if (state.is(BlockTags.CAULDRONS))
			state = Blocks.CAULDRON.defaultBlockState();

		if (world.dimensionType()
			.ultraWarm() && state.getFluidState().is(FluidTags.WATER)) {
			int i = target.getX();
			int j = target.getY();
			int k = target.getZ();
			world.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
				2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; ++l) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(),
					0.0D, 0.0D, 0.0D);
			}
			Block.dropResources(state, world, target);
			return;
		}

		if (state.getBlock() instanceof BaseRailBlock) {
			placeRailWithoutUpdate(world, state, target);
		} else if (AllBlocks.BELT.has(state)) {
			world.setBlock(target, state, 2);
		} else {
			world.setBlock(target, state, 18);
		}

		if (data != null) {
			if (existingBlockEntity instanceof IMergeableBE mergeable) {
				BlockEntity loaded = BlockEntity.loadStatic(target, state, data);
				if (existingBlockEntity.getType()
					.equals(loaded.getType())) {
					mergeable.accept(loaded);
					return;
				}
			}
			BlockEntity blockEntity = world.getBlockEntity(target);
			if (blockEntity != null) {
				data.putInt("x", target.getX());
				data.putInt("y", target.getY());
				data.putInt("z", target.getZ());
				if (blockEntity instanceof KineticBlockEntity)
					((KineticBlockEntity) blockEntity).warnOfMovement();
				blockEntity.load(data);
			}
		}

		try {
			state.getBlock()
				.setPlacedBy(world, target, state, null, stack);
		} catch (Exception e) {
		}
	}

	public static double getBounceMultiplier(Block block) {
		if (block instanceof SlimeBlock)
			return 0.8D;
		if (block instanceof BedBlock)
			return 0.66 * 0.8D;
		return 0;
	}

	public static boolean hasBlockSolidSide(BlockState p_220056_0_, BlockGetter p_220056_1_, BlockPos p_220056_2_,
		Direction p_220056_3_) {
		return !p_220056_0_.is(BlockTags.LEAVES)
			&& Block.isFaceFull(p_220056_0_.getCollisionShape(p_220056_1_, p_220056_2_), p_220056_3_);
	}

	public static boolean extinguishFire(Level world, @Nullable Player p_175719_1_, BlockPos p_175719_2_,
		Direction p_175719_3_) {
		p_175719_2_ = p_175719_2_.relative(p_175719_3_);
		if (world.getBlockState(p_175719_2_)
			.getBlock() == Blocks.FIRE) {
			world.levelEvent(p_175719_1_, 1009, p_175719_2_, 0);
			world.removeBlock(p_175719_2_, false);
			return true;
		} else {
			return false;
		}
	}

	public static BlockState copyProperties(BlockState fromState, BlockState toState) {
		for (Property<?> property : fromState.getProperties()) {
			toState = copyProperty(property, fromState, toState);
		}
		return toState;
	}

	public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState fromState,
		BlockState toState) {
		if (fromState.hasProperty(property) && toState.hasProperty(property)) {
			return toState.setValue(property, fromState.getValue(property));
		}
		return toState;
	}

	public static boolean isNotUnheated(BlockState state) {
		if (state.is(BlockTags.CAMPFIRES) && state.hasProperty(CampfireBlock.LIT)) {
			return state.getValue(CampfireBlock.LIT);
		}
		if (state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)) {
			return state.getValue(BlazeBurnerBlock.HEAT_LEVEL) != HeatLevel.NONE;
		}
		return true;
	}

}
