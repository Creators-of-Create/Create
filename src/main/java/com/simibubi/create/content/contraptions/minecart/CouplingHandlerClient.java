package com.simibubi.create.content.contraptions.minecart;

import com.mojang.math.Vector3f;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;

import net.createmod.catnip.utility.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CouplingHandlerClient {

	static AbstractMinecart selectedCart;
	static RandomSource r = RandomSource.create();

	public static void tick() {
		if (selectedCart == null)
			return;
		spawnSelectionParticles(selectedCart.getBoundingBox(), false);
		LocalPlayer player = Minecraft.getInstance().player;
		ItemStack heldItemMainhand = player.getMainHandItem();
		ItemStack heldItemOffhand = player.getOffhandItem();
		if (AllItems.MINECART_COUPLING.isIn(heldItemMainhand) || AllItems.MINECART_COUPLING.isIn(heldItemOffhand))
			return;
		selectedCart = null;
	}

	static void onCartClicked(Player player, AbstractMinecart entity) {
		if (Minecraft.getInstance().player != player)
			return;
		if (selectedCart == null || selectedCart == entity) {
			selectedCart = entity;
			spawnSelectionParticles(selectedCart.getBoundingBox(), true);
			return;
		}
		spawnSelectionParticles(entity.getBoundingBox(), true);
		AllPackets.getChannel().sendToServer(new CouplingCreationPacket(selectedCart, entity));
		selectedCart = null;
	}

	static void sneakClick() {
		selectedCart = null;
	}

	private static void spawnSelectionParticles(AABB AABB, boolean highlight) {
		ClientLevel world = Minecraft.getInstance().level;
		Vec3 center = AABB.getCenter();
		int amount = highlight ? 100 : 2;
		ParticleOptions particleData =
			highlight ? ParticleTypes.END_ROD : new DustParticleOptions(new Vector3f(1, 1, 1), 1);
		for (int i = 0; i < amount; i++) {
			Vec3 v = VecHelper.offsetRandomly(Vec3.ZERO, r, 1);
			double yOffset = v.y;
			v = v.multiply(1, 0, 1)
				.normalize()
				.add(0, yOffset / 8f, 0)
				.add(center);
			world.addParticle(particleData, v.x, v.y, v.z, 0, 0, 0);
		}
	}

}
