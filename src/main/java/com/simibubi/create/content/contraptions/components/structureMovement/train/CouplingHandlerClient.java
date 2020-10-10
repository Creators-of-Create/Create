package com.simibubi.create.content.contraptions.components.structureMovement.train;

import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class CouplingHandlerClient {

	static AbstractMinecartEntity selectedCart;
	static Random r = new Random();

	public static void tick() {
		if (selectedCart == null)
			return;
		spawnSelectionParticles(selectedCart.getBoundingBox(), false);
		ClientPlayerEntity player = Minecraft.getInstance().player;
		ItemStack heldItemMainhand = player.getHeldItemMainhand();
		ItemStack heldItemOffhand = player.getHeldItemOffhand();
		if (AllItems.MINECART_COUPLING.isIn(heldItemMainhand) || AllItems.MINECART_COUPLING.isIn(heldItemOffhand))
			return;
		selectedCart = null;
	}

	static void onCartClicked(PlayerEntity player, AbstractMinecartEntity entity) {
		if (Minecraft.getInstance().player != player)
			return;
		if (selectedCart == null || selectedCart == entity) {
			selectedCart = entity;
			spawnSelectionParticles(selectedCart.getBoundingBox(), true);
			return;
		}
		spawnSelectionParticles(entity.getBoundingBox(), true);
		AllPackets.channel.sendToServer(new CouplingCreationPacket(selectedCart, entity));
		selectedCart = null;
	}

	static void sneakClick() {
		selectedCart = null;
	}

	private static void spawnSelectionParticles(AxisAlignedBB axisAlignedBB, boolean highlight) {
		ClientWorld world = Minecraft.getInstance().world;
		Vector3d center = axisAlignedBB.getCenter();
		int amount = highlight ? 100 : 2;
		IParticleData particleData = highlight ? ParticleTypes.END_ROD : new RedstoneParticleData(1, 1, 1, 1);
		for (int i = 0; i < amount; i++) {
			Vector3d v = VecHelper.offsetRandomly(Vector3d.ZERO, r, 1);
			double yOffset = v.y;
			v = v.mul(1, 0, 1)
				.normalize()
				.add(0, yOffset / 8f, 0)
				.add(center);
			world.addParticle(particleData, v.x, v.y, v.z, 0, 0, 0);
		}
	}

}
