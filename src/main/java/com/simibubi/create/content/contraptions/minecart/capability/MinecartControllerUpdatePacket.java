package com.simibubi.create.content.contraptions.minecart.capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllAttachmentTypes;
import com.simibubi.create.AllPackets;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record MinecartControllerUpdatePacket(int entityId, @Nullable CompoundTag nbt) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, MinecartControllerUpdatePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MinecartControllerUpdatePacket::entityId,
			CatnipStreamCodecBuilders.nullable(ByteBufCodecs.COMPOUND_TAG), MinecartControllerUpdatePacket::nbt,
			MinecartControllerUpdatePacket::new
	);

	public MinecartControllerUpdatePacket(MinecartController controller, @NotNull HolderLookup.Provider registries) {
		this(controller.cart().getId(), controller.isEmpty() ? null : controller.serializeNBT(registries));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entityByID = player.clientLevel.getEntity(entityId);
		if (entityByID == null)
			return;
		if (entityByID.hasData(AllAttachmentTypes.MINECART_CONTROLLER)) {
			if (nbt == null) {
				entityByID.removeData(AllAttachmentTypes.MINECART_CONTROLLER);
			} else {
				MinecartController controller = entityByID.getData(AllAttachmentTypes.MINECART_CONTROLLER);
				controller.deserializeNBT(player.registryAccess(), nbt);
			}
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.MINECART_CONTROLLER;
	}
}
