package com.simibubi.create.content.curiosities.deco;

import java.util.Map;

import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.logistics.trains.entity.CarriageSyncData;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class SlidingDoorMovementBehaviour implements MovementBehaviour {

	@Override
	public boolean renderAsNormalTileEntity() {
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		StructureBlockInfo structureBlockInfo = context.contraption.getBlocks()
			.get(context.localPos);
		if (structureBlockInfo == null)
			return;
		boolean open = SlidingDoorTileEntity.isOpen(structureBlockInfo.state);

		if (!context.world.isClientSide())
			tickOpen(context, open);

		Map<BlockPos, BlockEntity> tes = context.contraption.presentTileEntities;
		if (!(tes.get(context.localPos) instanceof SlidingDoorTileEntity doorTE))
			return;
		boolean wasSettled = doorTE.animation.settled();
		doorTE.animation.chase(open ? 1 : 0, .15f, Chaser.LINEAR);
		doorTE.animation.tickChaser();

		if (!wasSettled && doorTE.animation.settled() && !open)
			context.world.playLocalSound(context.position.x, context.position.y, context.position.z,
				SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, .125f, 1, false);
	}

	protected void tickOpen(MovementContext context, boolean currentlyOpen) {
		boolean shouldOpen = shouldOpen(context);
		if (!shouldUpdate(context, shouldOpen))
			return;
		if (currentlyOpen == shouldOpen)
			return;

		BlockPos pos = context.localPos;
		Contraption contraption = context.contraption;

		StructureBlockInfo info = contraption.getBlocks()
			.get(pos);
		if (info == null || !info.state.hasProperty(DoorBlock.OPEN))
			return;

		toggleDoor(pos, contraption, info);

		if (shouldOpen)
			context.world.playSound(null, new BlockPos(context.position), SoundEvents.IRON_DOOR_OPEN,
				SoundSource.BLOCKS, .125f, 1);
	}

	private void toggleDoor(BlockPos pos, Contraption contraption, StructureBlockInfo info) {
		BlockState newState = info.state.cycle(DoorBlock.OPEN);
		contraption.entity.setBlock(pos, new StructureBlockInfo(info.pos, newState, info.nbt));

		BlockPos otherPos = newState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		info = contraption.getBlocks()
			.get(otherPos);
		if (info != null && info.state.hasProperty(DoorBlock.OPEN)) {
			newState = info.state.cycle(DoorBlock.OPEN);
			contraption.entity.setBlock(otherPos, new StructureBlockInfo(info.pos, newState, info.nbt));
			contraption.invalidateColliders();
		}
	}

	protected boolean shouldUpdate(MovementContext context, boolean shouldOpen) {
		if (context.firstMovement && shouldOpen)
			return false;
		if (!context.data.contains("Open")) {
			context.data.putBoolean("Open", shouldOpen);
			return true;
		}
		boolean wasOpen = context.data.getBoolean("Open");
		context.data.putBoolean("Open", shouldOpen);
		return wasOpen != shouldOpen;
	}

	protected boolean shouldOpen(MovementContext context) {
		if (context.contraption.entity instanceof CarriageContraptionEntity cce) {
			CarriageSyncData carriageData = cce.getCarriageData();
			if (Math.abs(carriageData.distanceToDestination) > 1)
				return false;
		}
		return context.motion.length() < 1 / 128f && !context.contraption.entity.isStalled();
	}

}
