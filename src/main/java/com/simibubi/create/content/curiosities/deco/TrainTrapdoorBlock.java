package com.simibubi.create.content.curiosities.deco;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public class TrainTrapdoorBlock extends TrapDoorBlock implements IWrenchable {

	public TrainTrapdoorBlock(Properties p_57526_) {
		super(p_57526_);
	}

	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		pState = pState.cycle(OPEN);
		pLevel.setBlock(pPos, pState, 2);
		if (pState.getValue(WATERLOGGED))
			pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		playSound(pPlayer, pLevel, pPos, pState.getValue(OPEN));
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}

}
