package com.simibubi.create.content.equipment.zapper;

import com.simibubi.create.CreateClient;

import net.minecraft.world.InteractionHand;

public class ZapperClient {

	public static void dontAnimateItem(InteractionHand hand) {
		CreateClient.ZAPPER_RENDER_HANDLER.dontAnimateItem(hand);
	}

}
