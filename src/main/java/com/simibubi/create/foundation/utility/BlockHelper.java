package com.simibubi.create.foundation.utility;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.Property;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class BlockHelper {

	@OnlyIn(Dist.CLIENT)
	public static void addReducedDestroyEffects(BlockState state, World worldIn, BlockPos pos,
		ParticleManager manager) {
		if (!(worldIn instanceof ClientWorld))
			return;
		ClientWorld world = (ClientWorld) worldIn;
		VoxelShape voxelshape = state.getShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		voxelshape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
			double d1 = Math.min(1.0D, x2 - x1);
			double d2 = Math.min(1.0D, y2 - y1);
			double d3 = Math.min(1.0D, z2 - z1);
			int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
			int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
			int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.random.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + x1;
						double d8 = d5 * d2 + y1;
						double d9 = d6 * d3 + z1;
						manager
							.add((new DiggingParticle(world, (double) pos.getX() + d7, (double) pos.getY() + d8,
								(double) pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state)).init(pos));
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

	public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
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
			int preferredSlot = player.inventory.selected;
			ItemStack itemstack = player.inventory.getItem(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setItem(preferredSlot,
					new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.inventory.getContainerSize(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.inventory.getItem(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setItem(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.inventory.add(new ItemStack(required));
			amountFound /= 2;
		}

		return amountFound;
	}

	public static ItemStack getRequiredItem(BlockState state) {
		ItemStack itemStack = new ItemStack(state.getBlock());
		if (itemStack.getItem() == Items.FARMLAND)
			itemStack = new ItemStack(Items.DIRT);
		else if (itemStack.getItem() == Items.GRASS_PATH)
			itemStack = new ItemStack(Items.GRASS_BLOCK);
		return itemStack;
	}

	public static void destroyBlock(World world, BlockPos pos, float effectChance) {
		destroyBlock(world, pos, effectChance, stack -> Block.popResource(world, pos, stack));
	}

	public static void destroyBlock(World world, BlockPos pos, float effectChance,
		Consumer<ItemStack> droppedItemCallback) {
		destroyBlockAs(world, pos, null, ItemStack.EMPTY, effectChance, droppedItemCallback);
	}

	public static void destroyBlockAs(World world, BlockPos pos, @Nullable PlayerEntity player, ItemStack usedTool,
		float effectChance, Consumer<ItemStack> droppedItemCallback) {
		FluidState fluidState = world.getFluidState(pos);
		BlockState state = world.getBlockState(pos);
		if (world.random.nextFloat() < effectChance)
			world.levelEvent(2001, pos, Block.getId(state));
		TileEntity tileentity = state.hasTileEntity() ? world.getBlockEntity(pos) : null;
		if (player != null) {
			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled())
				return;

			if (event.getExpToDrop() > 0 && world instanceof ServerWorld)
				state.getBlock()
					.popExperience((ServerWorld) world, pos, event.getExpToDrop());

			usedTool.mineBlock(world, state, pos, player);
			player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
		}

		if (world instanceof ServerWorld && world.getGameRules()
			.getBoolean(GameRules.RULE_DOBLOCKDROPS) && !world.restoringBlockSnapshots
			&& (player == null || !player.isCreative())) {
			for (ItemStack itemStack : Block.getDrops(state, (ServerWorld) world, pos, tileentity, player, usedTool))
				droppedItemCallback.accept(itemStack);
			state.spawnAfterBreak((ServerWorld) world, pos, ItemStack.EMPTY);
		}

		world.setBlockAndUpdate(pos, fluidState.createLegacyBlock());
	}

	public static boolean isSolidWall(IBlockReader reader, BlockPos fromPos, Direction toDirection) {
		return hasBlockSolidSide(reader.getBlockState(fromPos.relative(toDirection)), reader, fromPos.relative(toDirection),
			toDirection.getOpposite());
	}

	public static boolean noCollisionInSpace(IBlockReader reader, BlockPos pos) {
		return reader.getBlockState(pos)
			.getCollisionShape(reader, pos)
			.isEmpty();
	}

	private static void placeRailWithoutUpdate(World world, BlockState state, BlockPos target) {
		int i = target.getX() & 15;
		int j = target.getY();
		int k = target.getZ() & 15;
		Chunk chunk = world.getChunkAt(target);
		ChunkSection chunksection = chunk.getSections()[j >> 4];
		if (chunksection == Chunk.EMPTY_SECTION) {
			chunksection = new ChunkSection(j >> 4 << 4);
			chunk.getSections()[j >> 4] = chunksection;
		}
		BlockState old = chunksection.setBlockState(i, j & 15, k, state);
		chunk.markUnsaved();
		world.markAndNotifyBlock(target, chunk, old, state, 82, 512);

		world.setBlock(target, state, 82);
		world.neighborChanged(target, world.getBlockState(target.below()).getBlock(), target.below());
	}

	public static void placeSchematicBlock(World world, BlockState state, BlockPos target, ItemStack stack,
		@Nullable CompoundNBT data) {
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
			world.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
				2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

			for (int l = 0; l < 8; ++l) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, i + Math.random(), j + Math.random(), k + Math.random(),
					0.0D, 0.0D, 0.0D);
			}
			Block.dropResources(state, world, target);
			return;
		}

		if (state.getBlock() instanceof AbstractRailBlock) {
			placeRailWithoutUpdate(world, state, target);
		} else {
			world.setBlock(target, state, 18);
		}

		if (data != null) {
			TileEntity tile = world.getBlockEntity(target);
			if (tile != null) {
				data.putInt("x", target.getX());
				data.putInt("y", target.getY());
				data.putInt("z", target.getZ());
				if (tile instanceof KineticTileEntity)
					((KineticTileEntity) tile).warnOfMovement();
				tile.load(tile.getBlockState(), data);
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

	public static boolean hasBlockSolidSide(BlockState p_220056_0_, IBlockReader p_220056_1_, BlockPos p_220056_2_,
		Direction p_220056_3_) {
		return !p_220056_0_.is(BlockTags.LEAVES)
			&& Block.isFaceFull(p_220056_0_.getCollisionShape(p_220056_1_, p_220056_2_), p_220056_3_);
	}

	public static boolean extinguishFire(World world, @Nullable PlayerEntity p_175719_1_, BlockPos p_175719_2_,
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

	public static <T extends Comparable<T>> BlockState copyProperty(Property<T> property, BlockState fromState, BlockState toState) {
		if (fromState.hasProperty(property) && toState.hasProperty(property)) {
			return toState.setValue(property, fromState.getValue(property));
		}
		return toState;
	}

}
