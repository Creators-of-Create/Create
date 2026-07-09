package com.simibubi.create.content.equipment.zapper;

import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;

public abstract class ShootGadgetPacket implements ClientboundPacketPayload {
	protected final Vec3 location;
	protected final InteractionHand hand;
	protected final boolean self;

	public ShootGadgetPacket(Vec3 location, InteractionHand hand, boolean self) {
		this.location = location;
		this.hand = hand;
		this.self = self;
	}

	protected abstract void handleAdditional();

	protected abstract ShootableGadgetRenderHandler getHandler();

	@Override
	public void handle(Player player) {
		Entity renderViewEntity = Minecraft.getInstance()
				.getCameraEntity();
		if (renderViewEntity == null)
			return;
		if (renderViewEntity.position()
				.distanceTo(location) > 100)
			return;

		ShootableGadgetRenderHandler handler = getHandler();
		handleAdditional();
		if (self)
			handler.shoot(hand, location);
		else
			handler.playSound(hand, location);
	}
}
