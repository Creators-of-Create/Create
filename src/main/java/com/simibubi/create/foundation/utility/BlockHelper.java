package com.simibubi.create.foundation.utility;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class BlockHelper {

	@OnlyIn(Dist.CLIENT)
	public static void addReducedDestroyEffects(BlockState state, Level worldIn, BlockPos pos, ParticleEngine manager) {
		if (!(worldIn instanceof ClientLevel))
			return;
		ClientLevel world = (ClientLevel) worldIn;
		VoxelShape voxelshape = state.getShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		if (state.isAir())
			return;
		if (RenderProperties.get(state)
			.addDestroyEffects(state, worldIn, pos, manager))
			return;

		voxelshape.forAllBoxes((p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
			double d1 = Math.min(1.0D, p_172276_ - p_172273_);
			double d2 = Math.min(1.0D, p_172277_ - p_172274_);
			double d3 = Math.min(1.0D, p_172278_ - p_172275_);
			int i = Math.max(2, Mth.ceil(d1 / 0.25D));
			int j = Math.max(2, Mth.ceil(d2 / 0.25D));
			int k = Math.max(2, Mth.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.random.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + p_172273_;
						double d8 = d5 * d2 + p_172274_;
						double d9 = d6 * d3 + p_172275_;
						manager.add(new TerrainParticle(world, pos.getX() + d7, pos.getY() + d8, pos.getZ() + d9,
							d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state, pos).updateSprite(state, pos));
					}
				}
			}
		});
	}

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
		if (blockState.hasProperty(BlockStateProperties.LEVEL_CAULDRON))
			return blockState.setValue(BlockStateProperties.LEVEL_CAULDRON, 0);
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
		BlockEntity tileentity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
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
			for (ItemStack itemStack : Block.getDrops(state, (ServerLevel) world, pos, tileentity, player, usedTool))
				droppedItemCallback.accept(itemStack);
			state.spawnAfterBreak((ServerLevel) world, pos, ItemStack.EMPTY);
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
		int i = target.getX() & 15;
		int j = target.getY();
		int k = target.getZ() & 15;
		LevelChunk chunk = world.getChunkAt(target);
		LevelChunkSection chunksection = chunk.getSections()[j >> 4];
		if (chunksection == LevelChunk.EMPTY_SECTION) {
			chunksection = new LevelChunkSection(j >> 4 << 4);
			chunk.getSections()[j >> 4] = chunksection;
		}
		BlockState old = chunksection.setBlockState(i, j & 15, k, state);
		chunk.markUnsaved();
		world.markAndNotifyBlock(target, chunk, old, state, 82, 512);

		world.setBlock(target, state, 82);
		world.neighborChanged(target, world.getBlockState(target.below())
			.getBlock(), target.below());
	}

	public static void placeSchematicBlock(Level world, BlockState state, BlockPos target, ItemStack stack,
		@Nullable CompoundTag data) {
		// Piston
		if (state.hasProperty(BlockStateProperties.EXTENDED))
			state = state.setValue(BlockStateProperties.EXTENDED, Boolean.FALSE);
		if (state.hasProperty(BlockStateProperties.WATERLOGGED))
			state = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.FALSE);

		if (AllBlocks.BELT.has(state)) {
			world.setBlock(target, state, 2);
			return;
		} else if (state.getBlock() == Blocks.COMPOSTER)
			state = Blocks.COMPOSTER.defaultBlockState();
		else if (state.getBlock() != Blocks.SEA_PICKLE && state.getBlock() instanceof IPlantable)
			state = ((IPlantable) state.getBlock()).getPlant(world, target);

		if (world.dimensionType()
			.ultraWarm()
			&& state.getFluidState()
				.getType()
				.is(FluidTags.WATER)) {
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
		} else {
			world.setBlock(target, state, 18);
		}

		if (data != null) {
			BlockEntity tile = world.getBlockEntity(target);
			if (tile != null) {
				data.putInt("x", target.getX());
				data.putInt("y", target.getY());
				data.putInt("z", target.getZ());
				if (tile instanceof KineticTileEntity)
					((KineticTileEntity) tile).warnOfMovement();
				tile.load(data);
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
		if (block instanceof BedBlock || block instanceof SeatBlock)
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

}
