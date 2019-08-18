package com.simibubi.create.modules.gardens;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.simibubi.create.foundation.item.ItemWithToolTip;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class TreeFertilizerItem extends ItemWithToolTip {

	public TreeFertilizerItem(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemDescription getDescription() {
		return new ItemDescription(Palette.Green)
				.withSummary("A powerful combination of minerals suitable for common tree types.")
				.withBehaviour("When used on Sapling", "Grows Trees regardless of their spacing Conditions").createTabs();
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		BlockState state = context.getWorld().getBlockState(context.getPos());
		Block block = state.getBlock();
		if (block instanceof SaplingBlock) {

			if (context.getWorld().isRemote) {
				BoneMealItem.spawnBonemealParticles(context.getWorld(), context.getPos(), 100);
				return ActionResultType.SUCCESS;
			}

			TreesDreamWorld world = new TreesDreamWorld(context.getWorld());
			BlockPos saplingPos = context.getPos();

			for (BlockPos pos : BlockPos.getAllInBoxMutable(-1, 0, -1, 1, 0, 1)) {
				if (context.getWorld().getBlockState(saplingPos.add(pos)).getBlock() == block)
					world.setBlockState(pos.up(10), state.with(SaplingBlock.STAGE, 1));
			}

			((SaplingBlock) block).grow(world, BlockPos.ZERO.up(10), state.with(SaplingBlock.STAGE, 1), new Random());

			for (BlockPos pos : world.blocksAdded.keySet()) {
				BlockPos actualPos = pos.add(saplingPos).down(10);

				// Don't replace Bedrock
				if (context.getWorld().getBlockState(actualPos).getBlockHardness(context.getWorld(), actualPos) == -1)
					continue;
				// Don't replace solid blocks with leaves
				if (!world.getBlockState(pos).isNormalCube(world, pos)
						&& context.getWorld().getBlockState(actualPos).isNormalCube(context.getWorld(), actualPos))
					continue;
				if (world.getBlockState(pos).getBlock() == Blocks.PODZOL
						&& context.getWorld().getBlockState(actualPos).getBlock() != Blocks.GRASS_BLOCK)
					continue;

				context.getWorld().setBlockState(actualPos, world.getBlockState(pos));
			}

			if (!context.getPlayer().isCreative())
				context.getItem().shrink(1);
			return ActionResultType.SUCCESS;

		}

		return super.onItemUse(context);
	}

	private class TreesDreamWorld extends World {

		World wrapped;
		HashMap<BlockPos, BlockState> blocksAdded;

		protected TreesDreamWorld(World wrapped) {
			super(wrapped.getWorldInfo(), wrapped.dimension.getType(), (w, d) -> wrapped.getChunkProvider(),
					wrapped.getProfiler(), false);
			this.wrapped = wrapped;
			blocksAdded = new HashMap<>();
		}

		@Override
		public int getMaxHeight() {
			return 256;
		}

		@Override
		public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
			blocksAdded.put(pos, newState);
			return true;
		}

		@Override
		public boolean setBlockState(BlockPos pos, BlockState state) {
			return setBlockState(pos, state, 0);
		}

		@Override
		public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
			return p_217375_2_.test(getBlockState(p_217375_1_));
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			if (blocksAdded.containsKey(pos))
				return blocksAdded.get(pos);
			if (pos.getY() <= 9)
				return Blocks.GRASS_BLOCK.getDefaultState();
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public ITickList<Block> getPendingBlockTicks() {
			return EmptyTickList.get();
		}

		@Override
		public ITickList<Fluid> getPendingFluidTicks() {
			return EmptyTickList.get();
		}

		@Override
		public void playEvent(PlayerEntity player, int type, BlockPos pos, int data) {
		}

		@Override
		public List<? extends PlayerEntity> getPlayers() {
			return Collections.emptyList();
		}

		@Override
		public void notifyBlockUpdate(BlockPos pos, BlockState oldState, BlockState newState, int flags) {
		}

		@Override
		public void playSound(PlayerEntity player, double x, double y, double z, SoundEvent soundIn,
				SoundCategory category, float volume, float pitch) {
		}

		@Override
		public void playMovingSound(PlayerEntity p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_,
				SoundCategory p_217384_4_, float p_217384_5_, float p_217384_6_) {
		}

		@Override
		public Entity getEntityByID(int id) {
			return null;
		}

		@Override
		public MapData func_217406_a(String p_217406_1_) {
			return wrapped.func_217406_a(p_217406_1_);
		}

		@Override
		public void func_217399_a(MapData p_217399_1_) {
		}

		@Override
		public int getNextMapId() {
			return 0;
		}

		@Override
		public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
		}

		@Override
		public Scoreboard getScoreboard() {
			return null;
		}

		@Override
		public RecipeManager getRecipeManager() {
			return null;
		}

		@Override
		public NetworkTagManager getTags() {
			return null;
		}

	}

}
