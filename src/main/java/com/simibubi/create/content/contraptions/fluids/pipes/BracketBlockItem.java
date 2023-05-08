package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Optional;

import com.simibubi.create.content.contraptions.relays.elementary.BracketedBlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BracketBlockItem extends BlockItem {

	public BracketBlockItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		BracketBlock bracketBlock = getBracketBlock();
		Player player = context.getPlayer();

		BracketedBlockEntityBehaviour behaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);

		if (behaviour == null)
			return InteractionResult.FAIL;
		if (!behaviour.canHaveBracket())
			return InteractionResult.FAIL;
		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		Optional<BlockState> suitableBracket = bracketBlock.getSuitableBracket(state, context.getClickedFace());
		if (!suitableBracket.isPresent() && player != null)
			suitableBracket =
				bracketBlock.getSuitableBracket(state, Direction.orderedByNearest(player)[0].getOpposite());
		if (!suitableBracket.isPresent())
			return InteractionResult.SUCCESS;

		BlockState bracket = behaviour.getBracket();
		BlockState newBracket = suitableBracket.get();
		
		if (bracket == newBracket)
			return InteractionResult.SUCCESS;
		
		world.playSound(null, pos, newBracket
			.getSoundType()
			.getPlaceSound(), SoundSource.BLOCKS, 0.75f, 1);
		behaviour.applyBracket(newBracket);
		
		if (player == null || !player.isCreative()) {
			context.getItemInHand()
				.shrink(1);
			if (bracket != null) {
				ItemStack returnedStack = new ItemStack(bracket.getBlock());
				if (player == null)
					Block.popResource(world, pos, returnedStack);
				else
					player.getInventory().placeItemBackInInventory(returnedStack);
			}
		}
		return InteractionResult.SUCCESS;
	}

	private BracketBlock getBracketBlock() {
		return (BracketBlock) getBlock();
	}

}
