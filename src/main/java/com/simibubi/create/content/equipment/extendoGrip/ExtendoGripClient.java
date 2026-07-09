package com.simibubi.create.content.equipment.extendoGrip;

import java.util.function.Consumer;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.network.ClientNetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ExtendoGripClient {

	@SubscribeEvent
	public static void dontMissEntitiesWhenYouHaveHighReachDistance(InputEvent.InteractionKeyMappingTriggered event) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (mc.level == null || player == null)
			return;
		if (!ExtendoGripItem.isHoldingExtendoGrip(player))
			return;
		if (mc.hitResult instanceof BlockHitResult && mc.hitResult.getType() != Type.MISS)
			return;

		// Modified version of GameRenderer#getMouseOver.
		double distance = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
		if (!player.isCreative())
			distance -= 0.5f;
		Vec3 start = player.getEyePosition(AnimationTickHolder.getPartialTicks());
		Vec3 view = player.getViewVector(1.0F);
		Vec3 end = start.add(view.x * distance, view.y * distance, view.z * distance);
		AABB bounds = player.getBoundingBox()
			.expandTowards(view.scale(distance))
			.inflate(1.0D, 1.0D, 1.0D);
		EntityHitResult result = ProjectileUtil.getEntityHitResult(player, start, end, bounds,
			entity -> !entity.isSpectator() && entity.isPickable(), distance * distance);
		if (result != null) {
			Entity entity = result.getEntity();
			Vec3 hit = result.getLocation();
			double distanceToHit = start.distanceToSqr(hit);
			if (distanceToHit < distance * distance || mc.hitResult == null || mc.hitResult.getType() == Type.MISS) {
				mc.hitResult = result;
				if (entity instanceof LivingEntity || entity instanceof ItemFrame)
					mc.crosshairPickEntity = entity;
			}
		}
	}

	public static void sendInteractionPacket(Entity target) {
		ClientNetworkHelper.INSTANCE.sendToServer(new ExtendoGripInteractionPacket(target));
	}

	public static void sendInteractionPacket(Entity target, InteractionHand hand) {
		ClientNetworkHelper.INSTANCE.sendToServer(new ExtendoGripInteractionPacket(target, hand));
	}

	public static void sendInteractionPacket(Entity target, InteractionHand hand, Vec3 localPos) {
		ClientNetworkHelper.INSTANCE.sendToServer(new ExtendoGripInteractionPacket(target, hand, localPos));
	}

	private static boolean isUncaughtClientInteraction(Entity entity, Entity target) {
		// Server ignores entity interaction further than 6m.
		if (entity.distanceToSqr(target) < 36)
			return false;
		if (!entity.level().isClientSide())
			return false;
		if (!(entity instanceof Player))
			return false;
		return true;
	}

	@SubscribeEvent
	public static void notifyServerOfLongRangeAttacks(AttackEntityEvent event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (ExtendoGripItem.isHoldingExtendoGrip(player))
			sendInteractionPacket(target);
	}

	@SubscribeEvent
	public static void notifyServerOfLongRangeInteractions(PlayerInteractEvent.EntityInteract event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (ExtendoGripItem.isHoldingExtendoGrip(player))
			sendInteractionPacket(target, event.getHand());
	}

	@SubscribeEvent
	public static void notifyServerOfLongRangeSpecificInteractions(PlayerInteractEvent.EntityInteractSpecific event) {
		Entity entity = event.getEntity();
		Entity target = event.getTarget();
		if (!isUncaughtClientInteraction(entity, target))
			return;
		Player player = (Player) entity;
		if (ExtendoGripItem.isHoldingExtendoGrip(player))
			sendInteractionPacket(target, event.getHand(), event.getLocalPos());
	}

	public static void initializeClient(ExtendoGripItem item, Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(item, new ExtendoGripItemRenderer()));
	}

}
