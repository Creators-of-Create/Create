package com.simibubi.create.content.contraptions.components.actors;

import java.util.function.BiConsumer;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BellMovementBehaviour extends MovementBehaviour {

	private BiConsumer<World, BlockPos> soundPlayer;
	public static final BiConsumer<World, BlockPos> VANILLA_SOUND = (world, pos) -> {
		world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS,
				2.0F, 1.0F);
	};

	public BellMovementBehaviour(BiConsumer<World, BlockPos> soundPlayer) {
		this.soundPlayer = soundPlayer;
	}

	public BellMovementBehaviour() {
		this(VANILLA_SOUND);
	}

	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	public void onSpeedChanged(MovementContext context, Vector3d oldMotion, Vector3d motion) {
		double dotProduct = oldMotion.dotProduct(motion);

		if (dotProduct <= 0 && (context.relativeMotion.length() != 0) || context.firstMovement)
			soundPlayer.accept(context.world, new BlockPos(context.position));
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.position != null)
			soundPlayer.accept(context.world, new BlockPos(context.position));
	}
}
