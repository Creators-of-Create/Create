package com.simibubi.create.content.contraptions.processing.burner;

import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class LitBlazeBurnerBlock extends Block {

	public static final EnumProperty<FlameType> FLAME_TYPE = EnumProperty.create("flame_type", FlameType.class);

	public LitBlazeBurnerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FLAME_TYPE, FlameType.REGULAR));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FLAME_TYPE);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult blockRayTraceResult) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (heldItem.getToolTypes().contains(ToolType.SHOVEL)) {
			world.playSound(player, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5f, 2);
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
			world.setBlockAndUpdate(pos, AllBlocks.BLAZE_BURNER.getDefaultState());
			return ActionResultType.SUCCESS;
		}

		if (state.getValue(FLAME_TYPE) == FlameType.REGULAR) {
			if (heldItem.getItem().is(ItemTags.SOUL_FIRE_BASE_BLOCKS)) {
				world.playSound(player, pos, SoundEvents.SOUL_SAND_PLACE, SoundCategory.BLOCKS, 1.0f, world.random.nextFloat() * 0.4F + 0.8F);
				if (world.isClientSide)
					return ActionResultType.SUCCESS;
				world.setBlockAndUpdate(pos, defaultBlockState().setValue(FLAME_TYPE, FlameType.SOUL));
				return ActionResultType.SUCCESS;
			}
		}

		return ActionResultType.PASS;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
		return AllBlocks.BLAZE_BURNER.get()
			.getShape(state, reader, pos, context);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
		PlayerEntity player) {
		return AllItems.EMPTY_BLAZE_BURNER.asStack();
	}

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		world.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, true,
			(double) pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1),
			(double) pos.getY() + random.nextDouble() + random.nextDouble(),
			(double) pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), 0.0D,
			0.07D, 0.0D);

		if (random.nextInt(10) == 0) {
			world.playLocalSound((double) ((float) pos.getX() + 0.5F), (double) ((float) pos.getY() + 0.5F),
				(double) ((float) pos.getZ() + 0.5F), SoundEvents.CAMPFIRE_CRACKLE, SoundCategory.BLOCKS,
				0.25F + random.nextFloat() * .25f, random.nextFloat() * 0.7F + 0.6F, false);
		}

		if (state.getValue(FLAME_TYPE) == FlameType.SOUL) {
			if (random.nextInt(8) == 0) {
				world.addParticle(ParticleTypes.SOUL,
					pos.getX() + 0.5F + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
					pos.getY() + 0.3F + random.nextDouble() / 2,
					pos.getZ() + 0.5F + random.nextDouble() / 4 * (random.nextBoolean() ? 1 : -1),
					0.0, random.nextDouble() * 0.04 + 0.04, 0.0);
			}
			return;
		}

		if (random.nextInt(5) == 0) {
			for (int i = 0; i < random.nextInt(1) + 1; ++i) {
				world.addParticle(ParticleTypes.LAVA, (double) ((float) pos.getX() + 0.5F),
					(double) ((float) pos.getY() + 0.5F), (double) ((float) pos.getZ() + 0.5F),
					(double) (random.nextFloat() / 2.0F), 5.0E-5D, (double) (random.nextFloat() / 2.0F));
			}
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_149740_1_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, World p_180641_2_, BlockPos p_180641_3_) {
		return state.getValue(FLAME_TYPE) == FlameType.REGULAR ? 1 : 2;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos,
		ISelectionContext context) {
		return AllBlocks.BLAZE_BURNER.get()
			.getCollisionShape(state, reader, pos, context);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

	public static int getLight(BlockState state) {
		if (state.getValue(FLAME_TYPE) == FlameType.SOUL)
			return 9;
		else
			return 12;
	}

	public enum FlameType implements IStringSerializable {

		REGULAR, SOUL;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}

	}

}
