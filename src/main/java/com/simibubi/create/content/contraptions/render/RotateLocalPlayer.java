package com.simibubi.create.content.contraptions.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(value = Dist.CLIENT)
public abstract class RotateLocalPlayer {
	public static float deltaXROT = 0f;
	public static float deltaYROT = 0f;


	@SubscribeEvent
	public static void rotate(TickEvent.RenderTickEvent event) {
		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;

		float x = Mth.lerp(event.renderTickTime, player.getXRot(), player.getXRot() + deltaXROT);
		float y = Mth.lerp(event.renderTickTime, player.getYRot(), player.getYRot() + deltaYROT);
		player.setXRot(x);
		player.setYRot(y);
		deltaXROT *= (1 - event.renderTickTime);
		deltaYROT *= (1 - event.renderTickTime);
	}
}
