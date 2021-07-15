package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Optional;

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

import net.minecraft.item.Item.Properties;

public class BracketBlockItem extends BlockItem {

	public BracketBlockItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType useOn(ItemUseContext context) {
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		BracketBlock bracketBlock = getBracketBlock();
		PlayerEntity player = context.getPlayer();

		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);

		if (behaviour == null)
			return ActionResultType.FAIL;
		if (!behaviour.canHaveBracket())
			return ActionResultType.FAIL;
		if (world.isClientSide)
			return ActionResultType.SUCCESS;

		Optional<BlockState> suitableBracket = bracketBlock.getSuitableBracket(state, context.getClickedFace());
		if (!suitableBracket.isPresent() && player != null)
			suitableBracket =
				bracketBlock.getSuitableBracket(state, Direction.orderedByNearest(player)[0].getOpposite());
		if (!suitableBracket.isPresent())
			return ActionResultType.SUCCESS;

		BlockState bracket = behaviour.getBracket();
		behaviour.applyBracket(suitableBracket.get());
		
		if (!world.isClientSide && player != null)
			behaviour.triggerAdvancements(world, player, state);
		
		if (player == null || !player.isCreative()) {
			context.getItemInHand()
				.shrink(1);
			if (bracket != Blocks.AIR.defaultBlockState()) {
				ItemStack returnedStack = new ItemStack(bracket.getBlock());
				if (player == null)
					Block.popResource(world, pos, returnedStack);
				else
					player.inventory.placeItemBackInInventory(world, returnedStack);
			}
		}
		return ActionResultType.SUCCESS;
	}

	private BracketBlock getBracketBlock() {
		return (BracketBlock) getBlock();
	}

}
