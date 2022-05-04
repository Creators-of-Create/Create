package com.simibubi.create.content.logistics.trains.management.schedule;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ScheduleItemRetrieval {

	@SubscribeEvent
	public static void removeScheduleFromConductor(EntityInteract event) {
		Entity entity = event.getTarget();
		Player player = event.getPlayer();
		if (player == null || entity == null)
			return;

		Entity rootVehicle = entity.getRootVehicle();
		if (!(rootVehicle instanceof CarriageContraptionEntity))
			return;

		ItemStack itemStack = event.getItemStack();
		if (AllItems.SCHEDULE.isIn(itemStack) && entity instanceof Wolf wolf) {
			itemStack.getItem()
				.interactLivingEntity(itemStack, player, wolf, event.getHand());
			return;
		}

		if (player.level.isClientSide)
			return;
		if (event.getHand() == InteractionHand.OFF_HAND)
			return;

		CarriageContraptionEntity cce = (CarriageContraptionEntity) rootVehicle;
		Contraption contraption = cce.getContraption();
		if (!(contraption instanceof CarriageContraption cc))
			return;

		Train train = cce.getCarriage().train;
		if (train == null)
			return;
		if (train.runtime.getSchedule() == null)
			return;

		Integer seatIndex = contraption.getSeatMapping()
			.get(entity.getUUID());
		if (seatIndex == null)
			return;
		BlockPos seatPos = contraption.getSeats()
			.get(seatIndex);
		Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
		if (directions == null)
			return;

		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (!itemInHand.isEmpty()) {
			AllSoundEvents.DENY.playOnServer(player.level, player.blockPosition(), 1, 1);
			player.displayClientMessage(Lang.translate("schedule.remove_with_empty_hand"), true);
			event.setCanceled(true);
			return;
		}

		AllSoundEvents.playItemPickup(player);
		player.displayClientMessage(
			Lang.translate(
				train.runtime.isAutoSchedule ? "schedule.auto_removed_from_train" : "schedule.removed_from_train"),
			true);

		player.getInventory()
			.placeItemBackInInventory(train.runtime.returnSchedule());
//		player.setItemInHand(event.getHand(), train.runtime.returnSchedule());
		event.setCanceled(true);
		return;
	}

}
