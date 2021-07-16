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
	public ActionResultType use(BlockState pState, World world, BlockPos pos, PlayerEntity player,
		Hand pHandIn, BlockRayTraceResult pHit) {
		return onTileEntityUse(world, pos, te -> {
			if (!(te instanceof BrassTunnelTileEntity))
				return ActionResultType.PASS;
			BrassTunnelTileEntity bte = (BrassTunnelTileEntity) te;
			List<ItemStack> stacksOfGroup = bte.grabAllStacksOfGroup(world.isClientSide);
			if (stacksOfGroup.isEmpty())
				return ActionResultType.PASS;
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			for (ItemStack itemStack : stacksOfGroup) 
				player.inventory.placeItemBackInInventory(world, itemStack.copy());
			world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, .2f,
				1f + Create.RANDOM.nextFloat());
			return ActionResultType.SUCCESS;
		});
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.BRASS_TUNNEL.create();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld worldIn,
		BlockPos currentPos, BlockPos facingPos) {
		return super.updateShape(state, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public void onRemove(BlockState pState, World pWorldIn, BlockPos pPos, BlockState pNewState,
		boolean pIsMoving) {
		if (pState.hasTileEntity()
			&& (pState.getBlock() != pNewState.getBlock() || !pNewState.hasTileEntity())) {
			TileEntityBehaviour.destroy(pWorldIn, pPos, FilteringBehaviour.TYPE);
			withTileEntityDo(pWorldIn, pPos, te -> {
				if (te instanceof BrassTunnelTileEntity)
					Block.popResource(pWorldIn, pPos, ((BrassTunnelTileEntity) te).stackToDistribute);
			});
			pWorldIn.removeBlockEntity(pPos);
		}
	}

}
