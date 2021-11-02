package com.simibubi.create.content.curiosities.tools;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class BlueprintAssignCompleteRecipePacket extends SimplePacketBase {

	private ResourceLocation recipeID;

	public BlueprintAssignCompleteRecipePacket(ResourceLocation recipeID) {
		this.recipeID = recipeID;
	}

	public BlueprintAssignCompleteRecipePacket(FriendlyByteBuf buffer) {
		recipeID = buffer.readResourceLocation();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeResourceLocation(recipeID);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
				.enqueueWork(() -> {
					ServerPlayer player = context.get()
							.getSender();
					if (player == null)
						return;
					if (player.containerMenu instanceof BlueprintContainer) {
						BlueprintContainer c = (BlueprintContainer) player.containerMenu;
						player.getLevel()
								.getRecipeManager()
								.byKey(recipeID)
								.ifPresent(r -> BlueprintItem.assignCompleteRecipe(c.ghostInventory, r));
					}
				});
		context.get()
				.setPacketHandled(true);
	}

}
