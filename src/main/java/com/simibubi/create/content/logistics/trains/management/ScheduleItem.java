package com.simibubi.create.content.logistics.trains.management;

import com.simibubi.create.AllContainerTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraption;
import com.simibubi.create.content.logistics.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.schedule.Schedule;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
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
				NetworkHooks.openGui((ServerPlayer) player, this, buf -> {
					buf.writeItem(heldItem);
				});
			return InteractionResultHolder.success(heldItem);
		}
		return InteractionResultHolder.pass(heldItem);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget,
		InteractionHand pUsedHand) {
		InteractionResult pass = InteractionResult.PASS;

		if (!pStack.hasTag())
			return pass;
		if (!pStack.getTag()
			.contains("Schedule"))
			return pass;

		Schedule schedule = Schedule.fromTag(pStack.getTagElement("Schedule"));

		if (pInteractionTarget == null)
			return pass;
		Entity rootVehicle = pInteractionTarget.getRootVehicle();
		if (!(rootVehicle instanceof CarriageContraptionEntity))
			return pass;
		if (pPlayer.level.isClientSide)
			return InteractionResult.SUCCESS;
		CarriageContraptionEntity entity = (CarriageContraptionEntity) rootVehicle;
		Contraption contraption = entity.getContraption();
		if (contraption instanceof CarriageContraption cc) {
			Train train = cc.getCarriage().train;
			if (train == null)
				return InteractionResult.SUCCESS;
			if (train.heldForAssembly) {
				pPlayer.displayClientMessage(Lang.translate("schedule.train_still_assembling"), true);
				return InteractionResult.SUCCESS;
			}
			train.runtime.setSchedule(schedule, false);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		return new ScheduleContainer(AllContainerTypes.SCHEDULE.get(), id, inv, heldItem);
	}

	@Override
	public Component getDisplayName() {
		return new TranslatableComponent(getDescriptionId());
	}

	@Override
	public void fillItemCategory(CreativeModeTab pCategory, NonNullList<ItemStack> pItems) {}

}
