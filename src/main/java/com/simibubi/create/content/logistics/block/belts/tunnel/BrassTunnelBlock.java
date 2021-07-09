package com.simibubi.create.content.logistics.block.belts.tunnel;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BrassTunnelBlock extends BeltTunnelBlock {

	public BrassTunnelBlock(AbstractBlock.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onUse(BlockState p_225533_1_, World world, BlockPos pos, PlayerEntity player,
		Hand p_225533_5_, BlockRayTraceResult p_225533_6_) {
		return onTileEntityUse(world, pos, te -> {
			if (!(te instanceof BrassTunnelTileEntity))
				return ActionResultType.PASS;
			BrassTunnelTileEntity bte = (BrassTunnelTileEntity) te;
			List<ItemStack> stacksOfGroup = bte.grabAllStacksOfGroup(world.isRemote);
			if (stacksOfGroup.isEmpty())
				return ActionResultType.PASS;
			if (world.isRemote)
				return ActionResultType.SUCCESS;
			for (ItemStack itemStack : stacksOfGroup) 
				player.inventory.placeItemBackInInventory(world, itemStack.copy());
			world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f,
				1f + Create.RANDOM.nextFloat());
			return ActionResultType.SUCCESS;
		});
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BRASS_TUNNEL.create();
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		return super.updatePostPlacement(state, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public void onReplaced(BlockState p_196243_1_, World p_196243_2_, BlockPos p_196243_3_, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasTileEntity()
			&& (p_196243_1_.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasTileEntity())) {
			TileEntityBehaviour.destroy(p_196243_2_, p_196243_3_, FilteringBehaviour.TYPE);
			withTileEntityDo(p_196243_2_, p_196243_3_, te -> {
				if (te instanceof BrassTunnelTileEntity)
					Block.spawnAsEntity(p_196243_2_, p_196243_3_, ((BrassTunnelTileEntity) te).stackToDistribute);
			});
			p_196243_2_.removeTileEntity(p_196243_3_);
		}
	}

}
