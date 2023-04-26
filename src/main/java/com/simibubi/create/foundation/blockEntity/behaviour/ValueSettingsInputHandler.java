package com.simibubi.create.foundation.blockEntity.behaviour;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ValueSettingsInputHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		Level world = event.getWorld();
		BlockPos pos = event.getPos();
		Player player = event.getPlayer();
		InteractionHand hand = event.getHand();

		if (!canInteract(player))
			return;
		if (AllBlocks.CLIPBOARD.isIn(player.getMainHandItem()))
			return;
		if (!(world.getBlockEntity(pos)instanceof SmartBlockEntity sbe))
			return;

		if (event.getSide() == LogicalSide.CLIENT)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
				() -> () -> CreateClient.VALUE_SETTINGS_HANDLER.cancelIfWarmupAlreadyStarted(event));

		if (event.isCanceled())
			return;

		for (BlockEntityBehaviour behaviour : sbe.getAllBehaviours()) {
			if (!(behaviour instanceof ValueSettingsBehaviour valueSettingsBehaviour))
				continue;

			BlockHitResult ray = RaycastHelper.rayTraceRange(world, player, 10);
			if (ray == null)
				return;
			if (behaviour instanceof SidedFilteringBehaviour) {
				behaviour = ((SidedFilteringBehaviour) behaviour).get(ray.getDirection());
				if (behaviour == null)
					continue;
			}

			if (!valueSettingsBehaviour.isActive())
				continue;
			if (valueSettingsBehaviour.onlyVisibleWithWrench()
				&& !AllItemTags.WRENCH.matches(player.getItemInHand(hand)))
				continue;
			if (valueSettingsBehaviour.getSlotPositioning()instanceof ValueBoxTransform.Sided sidedSlot) {
				if (!sidedSlot.isSideActive(sbe.getBlockState(), ray.getDirection()))
					continue;
				sidedSlot.fromSide(ray.getDirection());
			}

			boolean fakePlayer = player instanceof FakePlayer;
			if (!valueSettingsBehaviour.testHit(ray.getLocation()) && !fakePlayer)
				continue;

			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);

			if (!valueSettingsBehaviour.acceptsValueSettings() || fakePlayer) {
				valueSettingsBehaviour.onShortInteract(player, hand, ray.getDirection());
				return;
			}

			if (event.getSide() == LogicalSide.CLIENT) {
				BehaviourType<?> type = behaviour.getType();
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateClient.VALUE_SETTINGS_HANDLER
					.startInteractionWith(pos, type, hand, ray.getDirection()));
			}

			return;
		}
	}

	public static boolean canInteract(Player player) {
		return player != null && !player.isSpectator() && !player.isShiftKeyDown();
	}

}
