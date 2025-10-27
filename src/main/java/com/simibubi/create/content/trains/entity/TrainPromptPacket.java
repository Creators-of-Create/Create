package com.simibubi.create.content.trains.entity;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.trains.TrainHUD;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record TrainPromptPacket(Component text, boolean shadow) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, TrainPromptPacket> STREAM_CODEC = StreamCodec.composite(
			ComponentSerialization.STREAM_CODEC, TrainPromptPacket::text,
			ByteBufCodecs.BOOL, TrainPromptPacket::shadow,
	        TrainPromptPacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		TrainHUD.currentPrompt = text;
		TrainHUD.currentPromptShadow = shadow;
		TrainHUD.promptKeepAlive = 30;
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.S_TRAIN_PROMPT;
	}
}
