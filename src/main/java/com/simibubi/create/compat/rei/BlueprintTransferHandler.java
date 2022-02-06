package com.simibubi.create.compat.rei;

import com.simibubi.create.content.curiosities.tools.BlueprintAssignCompleteRecipePacket;
import com.simibubi.create.content.curiosities.tools.BlueprintScreen;

import com.simibubi.create.foundation.networking.AllPackets;

import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.display.Display;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlueprintTransferHandler implements TransferHandler {
	@Override
	public Result handle(Context context) {
		if (context.getContainerScreen() instanceof BlueprintScreen blueprint) {
			Display d = context.getDisplay();
			if (d.getDisplayLocation().isPresent()) {
				if (d.getCategoryIdentifier().toString().equals("minecraft:plugins/crafting")) {
					if (context.isActuallyCrafting()) {
						AllPackets.channel.sendToServer(new BlueprintAssignCompleteRecipePacket(d.getDisplayLocation().get()));
						context.getMinecraft().setScreen(blueprint);
					}
					return Result.createSuccessful();
				}
			}

		}
		return Result.createNotApplicable();
	}
}
