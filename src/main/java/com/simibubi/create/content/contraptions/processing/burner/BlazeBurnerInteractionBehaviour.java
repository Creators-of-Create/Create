package com.simibubi.create.content.contraptions.processing.burner;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.contraptions.components.structureMovement.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.ScheduleItem;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class BlazeBurnerInteractionBehaviour extends MovingInteractionBehaviour {

	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand activeHand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		ItemStack itemInHand = player.getItemInHand(activeHand);
		if (!AllItems.SCHEDULE.isIn(itemInHand))
			return false;
		if (!(contraptionEntity instanceof CarriageContraptionEntity carriage))
			return false;
		Contraption contraption = carriage.getContraption();
		if (!(contraption instanceof CarriageContraption carriageContraption))
			return false;

		StructureBlockInfo info = carriageContraption.getBlocks()
			.get(localPos);
		if (info == null || !info.state.hasProperty(BlazeBurnerBlock.HEAT_LEVEL)
			|| info.state.getValue(BlazeBurnerBlock.HEAT_LEVEL) == HeatLevel.NONE)
			return false;

		Direction assemblyDirection = carriageContraption.getAssemblyDirection();
		for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis())) {
			if (carriageContraption.inControl(localPos, direction)) {

				Schedule schedule = ScheduleItem.getSchedule(itemInHand);
				if (schedule == null)
					return false;
				Train train = carriage.getCarriage().train;
				if (train == null)
					return false;
				if (train.heldForAssembly) {
					player.displayClientMessage(Lang.translate("schedule.train_still_assembling"), true);
					AllSoundEvents.DENY.playOnServer(player.level, player.blockPosition(), 1, 1);
					return true;
				}

				train.runtime.setSchedule(schedule, false);
				AllSoundEvents.CONFIRM.playOnServer(player.level, player.blockPosition(), 1, 1);
				player.displayClientMessage(Lang.translate("schedule.applied_to_train")
					.withStyle(ChatFormatting.GREEN), true);
				return true;
			}
		}

		player.displayClientMessage(Lang.translate("schedule.non_controlling_seat"), true);
		AllSoundEvents.DENY.playOnServer(player.level, player.blockPosition(), 1, 1);
		return true;
	}

}
