package com.simibubi.create.foundation.utility;

import java.util.function.Consumer;

import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.state.Property;
import net.minecraft.tags.BlockTags;
import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class BlockHelper {

	@OnlyIn(Dist.CLIENT)
	public static void addReducedDestroyEffects(BlockState state, World worldIn, BlockPos pos, ParticleManager manager) {
		if (!(worldIn instanceof ClientWorld))
			return;
		ClientWorld world = (ClientWorld) worldIn;
		VoxelShape voxelshape = state.getShape(world, pos);
		MutableInt amtBoxes = new MutableInt(0);
		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> amtBoxes.increment());
		double chance = 1d / amtBoxes.getValue();

		voxelshape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			double d1 = Math.min(1.0D, x2 - x1);
			double d2 = Math.min(1.0D, y2 - y1);
			double d3 = Math.min(1.0D, z2 - z1);
			int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
			int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
			int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

			for (int l = 0; l < i; ++l) {
				for (int i1 = 0; i1 < j; ++i1) {
					for (int j1 = 0; j1 < k; ++j1) {
						if (world.rand.nextDouble() > chance)
							continue;

						double d4 = ((double) l + 0.5D) / (double) i;
						double d5 = ((double) i1 + 0.5D) / (double) j;
						double d6 = ((double) j1 + 0.5D) / (double) k;
						double d7 = d4 * d1 + x1;
						double d8 = d5 * d2 + y1;
						double d9 = d6 * d3 + z1;
						manager
							.addEffect((new DiggingParticle(world, (double) pos.getX() + d7, (double) pos.getY() + d8,
								(double) pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, state)).setBlockPos(pos));
					}
				}
			}

		});
	}

	public static BlockState setZeroAge(BlockState blockState) {
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_1))
			return blockState.with(BlockStateProperties.AGE_0_1, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_2))
			return blockState.with(BlockStateProperties.AGE_0_2, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_3))
			return blockState.with(BlockStateProperties.AGE_0_3, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_5))
			return blockState.with(BlockStateProperties.AGE_0_5, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_7))
			return blockState.with(BlockStateProperties.AGE_0_7, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_15))
			return blockState.with(BlockStateProperties.AGE_0_15, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.AGE_0_25))
			return blockState.with(BlockStateProperties.AGE_0_25, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.HONEY_LEVEL))
			return blockState.with(BlockStateProperties.HONEY_LEVEL, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.HATCH_0_2))
			return blockState.with(BlockStateProperties.HATCH_0_2, 0);
		if (hasBlockStateProperty(blockState, BlockStateProperties.STAGE_0_1))
			return blockState.with(BlockStateProperties.STAGE_0_1, 0);
		return blockState;
	}

	public static int findAndRemoveInInventory(BlockState block, PlayerEntity player, int amount) {
		int amountFound = 0;
		Item required = getRequiredItem(block).getItem();

		boolean needsTwo =
			hasBlockStateProperty(block, BlockStateProperties.SLAB_TYPE) && block.get(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE;

		if (needsTwo)
			amount *= 2;

		if (hasBlockStateProperty(block, BlockStateProperties.EGGS_1_4))
			amount *= block.get(BlockStateProperties.EGGS_1_4);

		if (hasBlockStateProperty(block, BlockStateProperties.PICKLES_1_4))
			amount *= block.get(BlockStateProperties.PICKLES_1_4);

		{
			// Try held Item first
			int preferredSlot = player.inventory.currentItem;
			ItemStack itemstack = player.inventory.getStackInSlot(preferredSlot);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setInventorySlotContents(preferredSlot,
					new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		// Search inventory
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			if (amountFound == amount)
				break;

			ItemStack itemstack = player.inventory.getStackInSlot(i);
			int count = itemstack.getCount();
			if (itemstack.getItem() == required && count > 0) {
				int taken = Math.min(count, amount - amountFound);
				player.inventory.setInventorySlotContents(i, new ItemStack(itemstack.getItem(), count - taken));
				amountFound += taken;
			}
		}

		if (needsTwo) {
			// Give back 1 if uneven amount was removed
			if (amountFound % 2 != 0)
				player.inventory.addItemStackToInventory(new ItemStack(required));
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
		destroyBlock(world, pos, effectChance, stack -> Block.spawnAsEntity(world, pos, stack));
	}

	public static void destroyBlock(World world, BlockPos pos, float effectChance,
		Consumer<ItemStack> droppedItemCallback) {
		FluidState FluidState = world.getFluidState(pos);
		BlockState state = world.getBlockState(pos);
		if (world.rand.nextFloat() < effectChance)
			world.playEvent(2001, pos, Block.getStateId(state));
		TileEntity tileentity = state.hasTileEntity() ? world.getTileEntity(pos) : null;

		if (world.getGameRules()
			.getBoolean(GameRules.DO_TILE_DROPS) && !world.restoringBlockSnapshots && world instanceof ServerWorld) {
			for (ItemStack itemStack : Block.getDrops(state, (ServerWorld) world, pos, tileentity))
				droppedItemCallback.accept(itemStack);
			state.spawnAdditionalDrops((ServerWorld) world, pos, ItemStack.EMPTY);
		}

		world.setBlockState(pos, FluidState.getBlockState());
	}

	public static boolean isSolidWall(IBlockReader reader, BlockPos fromPos, Direction toDirection) {
		return hasBlockSolidSide(reader.getBlockState(fromPos.offset(toDirection)), reader,
			fromPos.offset(toDirection), toDirection.getOpposite());
	}
	
	public static boolean noCollisionInSpace(IBlockReader reader, BlockPos pos) {
		return reader.getBlockState(pos).getCollisionShape(reader, pos).isEmpty();
	}

	public static boolean hasBlockStateProperty(BlockState state, Property<?> p) {
		return state.method_28500(p).isPresent();
	}

	public static boolean hasBlockSolidSide(BlockState p_220056_0_, IBlockReader p_220056_1_, BlockPos p_220056_2_, Direction p_220056_3_) {
		return !p_220056_0_.isIn(BlockTags.LEAVES) && Block.doesSideFillSquare(p_220056_0_.getCollisionShape(p_220056_1_, p_220056_2_), p_220056_3_);
	}

	public static boolean extinguishFire(World world, @Nullable PlayerEntity p_175719_1_, BlockPos p_175719_2_, Direction p_175719_3_) {
		p_175719_2_ = p_175719_2_.offset(p_175719_3_);
		if (world.getBlockState(p_175719_2_).getBlock() == Blocks.FIRE) {
			world.playEvent(p_175719_1_, 1009, p_175719_2_, 0);
			world.removeBlock(p_175719_2_, false);
			return true;
		} else {
			return false;
		}
	}
}
