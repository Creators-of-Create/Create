package com.simibubi.create.content.curiosities.tools;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class BlueprintAssignCompleteRecipePacket extends SimplePacketBase {

	private ResourceLocation recipeID;

	public BlueprintAssignCompleteRecipePacket(ResourceLocation recipeID) {
		this.recipeID = recipeID;
	}

	public BlueprintAssignCompleteRecipePacket(PacketBuffer buffer) {
		recipeID = buffer.readResourceLocation();
	}

	@Override
	public void write(PacketBuffer buffer) {
		buffer.writeResourceLocation(recipeID);
	}

	@Override
	public void handle(Supplier<Context> context) {
		context.get()
				.enqueueWork(() -> {
					ServerPlayerEntity player = context.get()
							.getSender();
					if (player == null)
						return;
					if (player.openContainer instanceof BlueprintContainer) {
						BlueprintContainer c = (BlueprintContainer) player.openContainer;
						player.getServerWorld()
								.getRecipeManager()
								.getRecipe(recipeID)
								.ifPresent(r -> BlueprintItem.assignCompleteRecipe(c.ghostInventory, r));
					}
				});
		context.get()
				.setPacketHandled(true);
	}

}
