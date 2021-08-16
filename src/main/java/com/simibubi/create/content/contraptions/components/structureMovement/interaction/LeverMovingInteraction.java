package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

public class LeverMovingInteraction extends MovingInteractionBehaviour {
	@Override
	public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		BlockInfo info = contraptionEntity.getContraption().getBlocks().get(localPos);
		BlockState newState = info.state.cycle(BlockStateProperties.POWERED);
		setContraptionBlockData(contraptionEntity, localPos, new BlockInfo(info.pos, newState, info.nbt));
		player.getCommandSenderWorld().playSound(
			null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3f,
			newState.getValue(BlockStateProperties.POWERED) ? 0.6f : 0.5f
		);
		return true;
	}
}
