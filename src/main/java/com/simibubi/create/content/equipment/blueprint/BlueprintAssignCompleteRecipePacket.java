package com.simibubi.create.content.equipment.blueprint;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record BlueprintAssignCompleteRecipePacket(ResourceLocation recipeId) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(
			com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket::new, com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket::recipeId
	);

	@Override
	public void handle(ServerPlayer player) {
		if (player.containerMenu instanceof BlueprintMenu c) {
			player.level()
					.getRecipeManager()
					.byKey(recipeId)
					.ifPresent(r -> BlueprintItem.assignCompleteRecipe(c.player.level(), c.ghostInventory, r.value()));
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.BLUEPRINT_COMPLETE_RECIPE;
	}
}
