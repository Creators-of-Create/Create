package com.simibubi.create.content.curiosities.zapper;

import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.PacketDistributor;

public class ShootableGadgetItemMethods {

	public static void applyCooldown(PlayerEntity player, ItemStack item, Hand hand, Predicate<ItemStack> predicate,
		int cooldown) {
		boolean gunInOtherHand =
			predicate.test(player.getHeldItem(hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
		player.getCooldownTracker()
			.setCooldown(item.getItem(), gunInOtherHand ? cooldown * 2 / 3 : cooldown);
	}

	public static void sendPackets(PlayerEntity player, Function<Boolean, ? extends ShootGadgetPacket> factory) {
		if (!(player instanceof ServerPlayerEntity))
			return;
		AllPackets.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), factory.apply(false));
		AllPackets.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), factory.apply(true));
	}

	public static boolean shouldSwap(PlayerEntity player, ItemStack item, Hand hand, Predicate<ItemStack> predicate) {
		boolean isSwap = item.getTag()
			.contains("_Swap");
		boolean mainHand = hand == Hand.MAIN_HAND;
		boolean gunInOtherHand = predicate.test(player.getHeldItem(mainHand ? Hand.OFF_HAND : Hand.MAIN_HAND));

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
			player.getHeldItem(Hand.MAIN_HAND)
				.getTag()
				.remove("_Swap");
		player.setActiveHand(hand);
		return false;
	}

	public static Vector3d getGunBarrelVec(PlayerEntity player, boolean mainHand, Vector3d rightHandForward) {
		Vector3d start = player.getPositionVec()
			.add(0, player.getEyeHeight(), 0);
		float yaw = (float) ((player.rotationYaw) / -180 * Math.PI);
		float pitch = (float) ((player.rotationPitch) / -180 * Math.PI);
		int flip = mainHand == (player.getPrimaryHand() == HandSide.RIGHT) ? -1 : 1;
		Vector3d barrelPosNoTransform = new Vector3d(flip * rightHandForward.x, rightHandForward.y, rightHandForward.z);
		Vector3d barrelPos = start.add(barrelPosNoTransform.rotatePitch(pitch)
			.rotateYaw(yaw));
		return barrelPos;
	}

}
