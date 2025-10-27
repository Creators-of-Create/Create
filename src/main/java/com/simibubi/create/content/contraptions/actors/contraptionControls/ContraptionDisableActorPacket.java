package com.simibubi.create.content.contraptions.actors.contraptionControls;

import java.util.List;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.createmod.catnip.net.base.ClientboundPacketPayload;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public record ContraptionDisableActorPacket(int entityId, ItemStack filter, boolean enable) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ContraptionDisableActorPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, ContraptionDisableActorPacket::entityId,
			ItemStack.OPTIONAL_STREAM_CODEC, ContraptionDisableActorPacket::filter,
			ByteBufCodecs.BOOL, ContraptionDisableActorPacket::enable,
	        ContraptionDisableActorPacket::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer player) {
		Entity entityByID = player.clientLevel.getEntity(entityId);
		if (!(entityByID instanceof AbstractContraptionEntity ace))
			return;

		Contraption contraption = ace.getContraption();
		List<ItemStack> disabledActors = contraption.getDisabledActors();
		if (filter.isEmpty())
			disabledActors.clear();

		if (!enable) {
			disabledActors.add(filter);
			contraption.setActorsActive(filter, false);
			return;
		}

		disabledActors.removeIf(next -> ContraptionControlsMovement.isSameFilter(next, filter) || next.isEmpty());

		contraption.setActorsActive(filter, true);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.CONTRAPTION_ACTOR_TOGGLE;
	}
}
