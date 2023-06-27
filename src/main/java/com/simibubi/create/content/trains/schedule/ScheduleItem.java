package com.simibubi.create.content.trains.schedule;

import java.util.List;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.destination.DestinationInstruction;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class ScheduleItem extends Item implements MenuProvider {

	public ScheduleItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer)
				NetworkHooks.openScreen((ServerPlayer) player, this, buf -> {
					buf.writeItem(heldItem);
				});
			return InteractionResultHolder.success(heldItem);
		}
		return InteractionResultHolder.pass(heldItem);
	}

	public InteractionResult handScheduleTo(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget,
		InteractionHand pUsedHand) {
		InteractionResult pass = InteractionResult.PASS;

		Schedule schedule = getSchedule(pStack);
		if (schedule == null)
			return pass;
		if (pInteractionTarget == null)
			return pass;
		Entity rootVehicle = pInteractionTarget.getRootVehicle();
		if (!(rootVehicle instanceof CarriageContraptionEntity))
			return pass;
		if (pPlayer.level().isClientSide)
			return InteractionResult.SUCCESS;

		CarriageContraptionEntity entity = (CarriageContraptionEntity) rootVehicle;
		Contraption contraption = entity.getContraption();
		if (contraption instanceof CarriageContraption cc) {

			Train train = entity.getCarriage().train;
			if (train == null)
				return InteractionResult.SUCCESS;

			Integer seatIndex = contraption.getSeatMapping()
				.get(pInteractionTarget.getUUID());
			if (seatIndex == null)
				return InteractionResult.SUCCESS;
			BlockPos seatPos = contraption.getSeats()
				.get(seatIndex);
			Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
			if (directions == null) {
				pPlayer.displayClientMessage(Lang.translateDirect("schedule.non_controlling_seat"), true);
				AllSoundEvents.DENY.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
				return InteractionResult.SUCCESS;
			}

			if (train.runtime.getSchedule() != null) {
				AllSoundEvents.DENY.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
				pPlayer.displayClientMessage(Lang.translateDirect("schedule.remove_with_empty_hand"), true);
				return InteractionResult.SUCCESS;
			}

			if (schedule.entries.isEmpty()) {
				AllSoundEvents.DENY.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
				pPlayer.displayClientMessage(Lang.translateDirect("schedule.no_stops"), true);
				return InteractionResult.SUCCESS;
			}

			train.runtime.setSchedule(schedule, false);
			AllAdvancements.CONDUCTOR.awardTo(pPlayer);
			AllSoundEvents.CONFIRM.playOnServer(pPlayer.level(), pPlayer.blockPosition(), 1, 1);
			pPlayer.displayClientMessage(Lang.translateDirect("schedule.applied_to_train")
				.withStyle(ChatFormatting.GREEN), true);
			pStack.shrink(1);
			pPlayer.setItemInHand(pUsedHand, pStack.isEmpty() ? ItemStack.EMPTY : pStack);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		Schedule schedule = getSchedule(stack);
		if (schedule == null || schedule.entries.isEmpty())
			return;

		MutableComponent caret = Components.literal("> ").withStyle(ChatFormatting.GRAY);
		MutableComponent arrow = Components.literal("-> ").withStyle(ChatFormatting.GRAY);

		List<ScheduleEntry> entries = schedule.entries;
		for (int i = 0; i < entries.size(); i++) {
			boolean current = i == schedule.savedProgress && schedule.entries.size() > 1;
			ScheduleEntry entry = entries.get(i);
			if (!(entry.instruction instanceof DestinationInstruction destination))
				continue;
			ChatFormatting format = current ? ChatFormatting.YELLOW : ChatFormatting.GOLD;
			MutableComponent prefix = current ? arrow : caret;
			tooltip.add(prefix.copy()
				.append(Components.literal(destination.getFilter()).withStyle(format)));
		}
	}

	public static Schedule getSchedule(ItemStack pStack) {
		if (!pStack.hasTag())
			return null;
		if (!pStack.getTag()
			.contains("Schedule"))
			return null;
		return Schedule.fromTag(pStack.getTagElement("Schedule"));
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		return new ScheduleMenu(AllMenuTypes.SCHEDULE.get(), id, inv, heldItem);
	}

	@Override
	public Component getDisplayName() {
		return getDescription();
	}

}
