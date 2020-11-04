package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Optional;

import com.simibubi.create.content.contraptions.fluids.FluidPipeAttachmentBehaviour;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BracketBlockItem extends BlockItem {

	public BracketBlockItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState state = world.getBlockState(pos);
		BracketBlock bracketBlock = getBracketBlock();
		PlayerEntity player = context.getPlayer();

		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, FluidPipeAttachmentBehaviour.TYPE);
		if (behaviour == null)
			behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);

		if (behaviour != null && behaviour.canHaveBracket()) {
			if (world.isRemote)
				return ActionResultType.SUCCESS;

			Optional<BlockState> suitableBracket = bracketBlock.getSuitableBracket(state, context.getFace());
			if (!suitableBracket.isPresent() && player != null)
				suitableBracket =
					bracketBlock.getSuitableBracket(state, Direction.getFacingDirections(player)[0].getOpposite());
			if (!suitableBracket.isPresent())
				return ActionResultType.SUCCESS;

			BlockState bracket = behaviour.getBracket();
			behaviour.applyBracket(suitableBracket.get());
			if (player == null || !player.isCreative()) {
				context.getItem()
					.shrink(1);
				if (bracket != Blocks.AIR.getDefaultState()) {
					ItemStack returnedStack = new ItemStack(bracket.getBlock());
					if (player == null)
						Block.spawnAsEntity(world, pos, returnedStack);
					else
						player.inventory.placeItemBackInInventory(world, returnedStack);
				}
			}
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.FAIL;
	}

	private BracketBlock getBracketBlock() {
		return (BracketBlock) getBlock();
	}

}
