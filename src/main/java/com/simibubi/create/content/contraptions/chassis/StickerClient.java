package com.simibubi.create.content.contraptions.chassis;

import com.simibubi.create.AllSoundEvents;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class StickerClient {

	public static void playSound(StickerBlockEntity blockEntity, boolean attach) {
		Player player = Minecraft.getInstance().player;
		AllSoundEvents.SLIME_ADDED.play(blockEntity.getLevel(), player, blockEntity.getBlockPos(), 0.35f, attach ? 0.75f : 0.2f);
	}

	public static void queueUpdate(StickerBlockEntity blockEntity) {
		VisualizationHelper.queueUpdate(blockEntity);
	}

}
