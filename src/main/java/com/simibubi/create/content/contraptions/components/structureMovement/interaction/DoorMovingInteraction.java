package com.simibubi.create.content.contraptions.components.structureMovement.interaction;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class DoorMovingInteraction extends SimpleBlockMovingInteraction {

	@Override
	protected BlockState handle(Player player, Contraption contraption, BlockPos pos, BlockState currentState) {
		SoundEvent sound =
			currentState.getValue(DoorBlock.OPEN) ? SoundEvents.WOODEN_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_OPEN;

		BlockPos otherPos = currentState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		StructureBlockInfo info = contraption.getBlocks()
			.get(otherPos);
		if (info.state.hasProperty(DoorBlock.OPEN))
			setContraptionBlockData(contraption.entity, otherPos,
				new StructureBlockInfo(info.pos, info.state.cycle(DoorBlock.OPEN), info.nbt));

		float pitch = player.level.random.nextFloat() * 0.1F + 0.9F;
		playSound(player, sound, pitch);
		return currentState.cycle(DoorBlock.OPEN);
	}

	@Override
	protected boolean updateColliders() {
		return true;
	}

}
