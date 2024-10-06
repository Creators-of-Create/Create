package com.simibubi.create.content.processing.burner;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.api.contraption.train.TrainConductorHandler;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovingInteractionBehaviour;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.function.Predicate;

public class BlockBasedTrainConductorInteractionBehaviour extends MovingInteractionBehaviour {

	private final Predicate<BlockState> isValidConductor;
	private final TrainConductorHandler.UpdateScheduleCallback callback;

	public BlockBasedTrainConductorInteractionBehaviour(Predicate<BlockState> isValidConductor, TrainConductorHandler.UpdateScheduleCallback callback) {
		this.isValidConductor = isValidConductor;
		this.callback = callback;
	}

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		ItemStack itemInHand = player.getItemInHand(activeHand);

		if (!(contraptionEntity instanceof CarriageContraptionEntity carriageEntity))
			return false;
		if (activeHand == InteractionHand.OFF_HAND)
			return false;
		Contraption contraption = carriageEntity.getContraption();
		if (!(contraption instanceof CarriageContraption carriageContraption))
			return false;

		StructureBlockInfo info = carriageContraption.getBlocks()
			.get(localPos);
		if (info == null || !isValidConductor.test(info.state()))
			return false;

		Direction assemblyDirection = carriageContraption.getAssemblyDirection();
		for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis())) {
			if (!carriageContraption.inControl(localPos, direction))
				continue;

			Train train = carriageEntity.getCarriage().train;
			if (train == null)
				return false;
			if (player.level().isClientSide)
				return true;

			if (train.runtime.getSchedule() != null) {
				if (train.runtime.paused && !train.runtime.completed) {
					train.runtime.paused = false;
					AllSoundEvents.CONFIRM.playOnServer(player.level(), player.blockPosition(), 1, 1);
					player.displayClientMessage(Lang.translateDirect("schedule.continued"), true);
					return true;
				}

				if (!itemInHand.isEmpty()) {
					AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
					player.displayClientMessage(Lang.translateDirect("schedule.remove_with_empty_hand"), true);
					return true;
				}

				AllSoundEvents.playItemPickup(player);
				player.displayClientMessage(Lang.translateDirect(
					train.runtime.isAutoSchedule ? "schedule.auto_removed_from_train" : "schedule.removed_from_train"),
					true);
				player.setItemInHand(activeHand, train.runtime.returnSchedule());
				callback.update(false, info.state(), newBlockState -> setBlockState(localPos, contraptionEntity, newBlockState));
				return true;
			}

			if (!AllItems.SCHEDULE.isIn(itemInHand))
				return true;

			Schedule schedule = ScheduleItem.getSchedule(itemInHand);
			if (schedule == null)
				return false;

			if (schedule.entries.isEmpty()) {
				AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
				player.displayClientMessage(Lang.translateDirect("schedule.no_stops"), true);
				return true;
			}
			callback.update(true, info.state(), newBlockState -> setBlockState(localPos, contraptionEntity, newBlockState));
			train.runtime.setSchedule(schedule, false);
			AllAdvancements.CONDUCTOR.awardTo(player);
			AllSoundEvents.CONFIRM.playOnServer(player.level(), player.blockPosition(), 1, 1);
			player.displayClientMessage(Lang.translateDirect("schedule.applied_to_train")
				.withStyle(ChatFormatting.GREEN), true);
			itemInHand.shrink(1);
			player.setItemInHand(activeHand, itemInHand.isEmpty() ? ItemStack.EMPTY : itemInHand);
			return true;
		}

		player.displayClientMessage(Lang.translateDirect("schedule.non_controlling_seat"), true);
		AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
		return true;
	}

	private void setBlockState(BlockPos localPos, AbstractContraptionEntity contraption, BlockState newState) {
		StructureTemplate.StructureBlockInfo info = contraption.getContraption().getBlocks().get(localPos);
		if (info != null) {
			setContraptionBlockData(contraption, localPos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));
		}
	}
}
