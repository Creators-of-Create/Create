package com.simibubi.create.content.contraptions.components.structureMovement;

import com.simibubi.create.CreateClient;

import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;

public class LeverMovingInteraction extends MovingInteractionBehaviour {
	@Override
	public boolean handlePlayerInteraction(PlayerEntity player, Hand activeHand, BlockPos localPos, AbstractContraptionEntity contraptionEntity) {
		Template.BlockInfo info = contraptionEntity.contraption.blocks.get(localPos);
		BlockState newState = info.state.cycle(BlockStateProperties.POWERED);
		contraptionEntity.contraption.blocks.put(localPos, new Template.BlockInfo(info.pos, newState, info.nbt));
		player.getCommandSenderWorld().playSound(
			null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundCategory.BLOCKS, 0.3f,
			newState.getValue(BlockStateProperties.POWERED) ? 0.6f : 0.5f
		);
		// mark contraption to re-render
		ContraptionRenderDispatcher.invalidate(contraptionEntity.contraption);
		return true;
	}
}
