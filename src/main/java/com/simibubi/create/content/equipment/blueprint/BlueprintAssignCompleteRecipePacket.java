package com.simibubi.create.content.equipment.blueprint;

import com.simibubi.create.AllPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

public record BlueprintAssignCompleteRecipePacket(Identifier recipeId) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket> STREAM_CODEC = Identifier.STREAM_CODEC.map(
			com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket::new, com.simibubi.create.content.equipment.blueprint.BlueprintAssignCompleteRecipePacket::recipeId
	);

	@Override
	public void handle(ServerPlayer player) {
		if (player.containerMenu instanceof BlueprintMenu c) {
			player.level().getServer()
					.getRecipeManager()
					.byKey(ResourceKey.create(Registries.RECIPE, recipeId))
					.ifPresent(r -> BlueprintItem.assignCompleteRecipe(c.player.level(), c.ghostInventory, r.value()));
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return AllPackets.BLUEPRINT_COMPLETE_RECIPE;
	}
}
