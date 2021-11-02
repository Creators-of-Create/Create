package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BrassTunnelBlock extends BeltTunnelBlock {

	public BrassTunnelBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState p_225533_1_, Level world, BlockPos pos, Player player,
		InteractionHand p_225533_5_, BlockHitResult p_225533_6_) {
		return onTileEntityUse(world, pos, te -> {
			if (!(te instanceof BrassTunnelTileEntity))
				return InteractionResult.PASS;
			BrassTunnelTileEntity bte = (BrassTunnelTileEntity) te;
			List<ItemStack> stacksOfGroup = bte.grabAllStacksOfGroup(world.isClientSide);
			if (stacksOfGroup.isEmpty())
				return InteractionResult.PASS;
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			for (ItemStack itemStack : stacksOfGroup) 
				player.inventory.placeItemBackInInventory(world, itemStack.copy());
			world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f,
				1f + Create.RANDOM.nextFloat());
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.BRASS_TUNNEL.create();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		return super.updateShape(state, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public void onRemove(BlockState p_196243_1_, Level p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(p_196243_2_, p_196243_3_, FilteringBehaviour.TYPE);
			withTileEntityDo(p_196243_2_, p_196243_3_, te -> {
				if (te instanceof BrassTunnelTileEntity)
					Block.popResource(p_196243_2_, p_196243_3_, ((BrassTunnelTileEntity) te).stackToDistribute);
			});
			p_196243_2_.removeBlockEntity(p_196243_3_);
		}
	}

}
