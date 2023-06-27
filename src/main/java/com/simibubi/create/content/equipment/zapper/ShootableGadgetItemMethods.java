package com.simibubi.create.content.equipment.zapper;

import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.AllPackets;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class ShootableGadgetItemMethods {

	public static void applyCooldown(Player player, ItemStack item, InteractionHand hand, Predicate<ItemStack> predicate,
		int cooldown) {
		if (cooldown <= 0)
			return;

		boolean gunInOtherHand =
			predicate.test(player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
		player.getCooldowns()
			.addCooldown(item.getItem(), gunInOtherHand ? cooldown * 2 / 3 : cooldown);
	}

	public static void sendPackets(Player player, Function<Boolean, ? extends ShootGadgetPacket> factory) {
		if (!(player instanceof ServerPlayer))
			return;
		AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> player), factory.apply(false));
		AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), factory.apply(true));
	}

	public static boolean shouldSwap(Player player, ItemStack item, InteractionHand hand, Predicate<ItemStack> predicate) {
		boolean isSwap = item.getTag()
			.contains("_Swap");
		boolean mainHand = hand == InteractionHand.MAIN_HAND;
		boolean gunInOtherHand = predicate.test(player.getItemInHand(mainHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));

		// Pass To Offhand
		if (mainHand && isSwap && gunInOtherHand)
			return true;
		if (mainHand && !isSwap && gunInOtherHand)
			item.getTag()
				.putBoolean("_Swap", true);
		if (!mainHand && isSwap)
			item.getTag()
				.remove("_Swap");
		if (!mainHand && gunInOtherHand)
			player.getItemInHand(InteractionHand.MAIN_HAND)
				.getTag()
				.remove("_Swap");
		player.startUsingItem(hand);
		return false;
	}

	public static Vec3 getGunBarrelVec(Player player, boolean mainHand, Vec3 rightHandForward) {
		Vec3 start = player.position()
			.add(0, player.getEyeHeight(), 0);
		float yaw = (float) ((player.getYRot()) / -180 * Math.PI);
		float pitch = (float) ((player.getXRot()) / -180 * Math.PI);
		int flip = mainHand == (player.getMainArm() == HumanoidArm.RIGHT) ? -1 : 1;
		Vec3 barrelPosNoTransform = new Vec3(flip * rightHandForward.x, rightHandForward.y, rightHandForward.z);
		Vec3 barrelPos = start.add(barrelPosNoTransform.xRot(pitch)
			.yRot(yaw));
		return barrelPos;
	}

}
