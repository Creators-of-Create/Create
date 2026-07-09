package com.simibubi.create.content.logistics.tunnel;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BrassTunnelBlock extends BeltTunnelBlock {

	public BrassTunnelBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		return onBlockEntityUse(level, pos, be -> {
			if (!(be instanceof BrassTunnelBlockEntity bte))
				return InteractionResult.PASS;
			List<ItemStack> stacksOfGroup = bte.grabAllStacksOfGroup(level.isClientSide());
			if (stacksOfGroup.isEmpty())
				return InteractionResult.PASS;
			if (level.isClientSide())
				return InteractionResult.SUCCESS;
			for (ItemStack itemStack : stacksOfGroup)
				player.getInventory().placeItemBackInInventory(itemStack.copy());
			level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
				1f + level.getRandom().nextFloat());
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public BlockEntityType<? extends BeltTunnelBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.BRASS_TUNNEL.get();
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader worldIn, ScheduledTickAccess ticks,
								  BlockPos currentPos, Direction facing, BlockPos facingPos,
								  BlockState facingState, RandomSource random) {
		return super.updateShape(state, worldIn, ticks, currentPos, facing, facingPos, facingState, random);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
		IBE.onRemove(state, level, pos, Blocks.AIR.defaultBlockState());
		super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
	}

}
